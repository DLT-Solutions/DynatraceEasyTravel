package com.dynatrace.easytravel.launcher.rap;

import static org.junit.Assert.*;

import org.eclipse.rap.rwt.testfixture.TestContext;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.junit.Rule;
import org.junit.Test;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.utils.TestHelpers;
import com.dynatrace.easytravel.weblauncher.LauncherUIRAP;


public class RAPLinkTest {
	@Rule
	public TestContext context = new TestContext();

    @Test
    public void testRAPLink() {
		TestHelpers.assumeCanUseDisplay();
        RAPLink rapLink = new RAPLink( new Shell(new Display()), 0, 0, 0 );
        assertTrue(rapLink.control instanceof Link);
        rapLink.setVisible(true);
        rapLink.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -7505682517100561827L;
		});
        assertFalse(rapLink.isDisposed());
        assertNotNull(rapLink.getParent());
        rapLink.setFont(null);
        rapLink.setText("123", false);
        rapLink.setToolTipText("tooltip");

        Launcher.addLauncherUI(new LauncherUIRAP());
        Launcher.setIsWeblauncher(true);
        callRAPLink();
    }

	protected void callRAPLink() {
		RAPLink rapLink = new RAPLink(new Shell(), 0, 0, 0);
        assertTrue(rapLink.control instanceof Label);
        rapLink.setVisible(true);
        rapLink.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -630726047292770275L;
		});
        assertFalse(rapLink.isDisposed());
        assertNotNull(rapLink.getParent());
        rapLink.setFont(null);
        rapLink.setText("123", false);
        rapLink.setToolTipText("tooltip");

        rapLink.setText("234asdlA@$TARAERGAERGA$QA#%TA$%T\"\"\\:&234&uemasdlkj223&@#?!@#$!#?!#$((*@$", false);

        rapLink.dispose();
	}
}
