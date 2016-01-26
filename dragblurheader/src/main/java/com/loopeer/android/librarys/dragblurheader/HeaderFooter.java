package com.loopeer.android.librarys.dragblurheader;

public interface HeaderFooter {

    void onMoveStart(int currentPosY, int startSwitchOffset, CharSequence string);

    void onCanStartSwitch(int currentPosY, int startSwitchOffset, CharSequence string);

}
