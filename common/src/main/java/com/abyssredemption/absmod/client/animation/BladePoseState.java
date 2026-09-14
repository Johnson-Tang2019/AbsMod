package com.abyssredemption.absmod.client.animation;

public interface BladePoseState {
    BladeMotionPreset.Pose absmod$getBladePose();
    void absmod$setBladePose(BladeMotionPreset.Pose pose, boolean leftHanded);
    boolean absmod$isLeftHanded();
}
