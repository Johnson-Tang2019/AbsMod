import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Remap cookware material pixels while retaining the upstream UV layout and transparency. */
class CookeryPalette {
    public static void main(String[] args) throws Exception {
        for (int i = 2; i < args.length; i++) {
            var original = ImageIO.read(Path.of(args[0], args[i] + ".png").toFile());
            var result = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < original.getHeight(); y++) for (int x = 0; x < original.getWidth(); x++) {
                int pixel = original.getRGB(x,y);
                int r = pixel >> 16 & 255, g = pixel >> 8 & 255, b = pixel & 255;
                double v = Math.pow((r*.30 + g*.59 + b*.11)/255, .74);
                boolean flame = args[i].contains("lit") && r > 180 && r > b + 90 && g > b + 35;
                boolean contents = (args[i].contains("teacup/") || args[i].contains("has_items")) && (g > r+12 || b > r+14);
                if ((pixel >>> 24) != 0 && !flame && !contents) {
                    r=(int)(97+158*Math.pow(v,.48)); g=(int)(47+183*Math.pow(v,.85)); b=(int)(75+167*Math.pow(v,.78));
                    pixel = pixel & 0xff000000 | r << 16 | g << 8 | b;
                }
                result.setRGB(x,y,pixel);
            }
            var out = Path.of(args[1], args[i]+".png");
            Files.createDirectories(out.getParent());
            ImageIO.write(result,"PNG",out.toFile());
        }
    }
}
