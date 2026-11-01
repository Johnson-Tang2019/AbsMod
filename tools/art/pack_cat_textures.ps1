# Pack AI-generated UV atlases into the two version-specific texture sizes.
param([string]$SourceDirectory = 'art/cats/genetics/source')
Add-Type -AssemblyName System.Drawing
$destination = 'common/src/main/resources/assets/absmod/textures/entity/cat'
New-Item -ItemType Directory -Force $destination | Out-Null
foreach ($name in @('chocolate','lilac','seal_point','blue_point','chocolate_point','lilac_point')) {
    $atlas = [System.Drawing.Bitmap]::new((Join-Path (Resolve-Path $SourceDirectory) "$name.png"))
    foreach ($baby in @($false,$true)) {
        $width = if ($baby) {32} else {64}
        $offset = if ($baby) {32} else {0}
        $suffix = if ($baby) {'_baby'} else {''}
        $output = [System.Drawing.Bitmap]::new($width,32)
        for ($y=0;$y -lt 32;$y++) {
            for ($x=0;$x -lt $width;$x++) {
                $px = [int][Math]::Floor(($x+0.5)*$atlas.Width/64)
                $py = [int][Math]::Floor(($y+$offset+0.5)*$atlas.Height/64)
                $output.SetPixel($x,$y,$atlas.GetPixel($px,$py))
            }
        }
        $output.Save((Join-Path (Resolve-Path $destination) "$name$suffix.png"))
        $output.Dispose()
    }
    $atlas.Dispose()
}
