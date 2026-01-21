package com.lx862.pwgui.gui.components.kui;

import com.lx862.pwgui.gui.NameTabPair;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class KTabbedPane extends JTabbedPane {

    /** This clears and sets the tab to the new tab. If the title and order matches the old one, the currently selected tab index will be restored */
    public void setTabs(List<NameTabPair> newTab) {
        final List<String> oldTabTitles = getTabTitles();
        final int oldSelected = getSelectedIndex();

        removeAll();

        for(NameTabPair nameTabPair : newTab) {
            add(nameTabPair.title(), nameTabPair.component());
        }

        final List<String> newTitles = getTabTitles();
        final boolean tabUnchanged = oldTabTitles.equals(newTitles);

        if(tabUnchanged && oldSelected != -1) {
            setSelectedIndex(oldSelected);
        }
    }

    private List<String> getTabTitles() {
        final List<String> titles = new ArrayList<>();
        for(int i = 0; i < getTabCount(); i++) {
            titles.add(getTitleAt(i));
        }
        return titles;
    }
}
