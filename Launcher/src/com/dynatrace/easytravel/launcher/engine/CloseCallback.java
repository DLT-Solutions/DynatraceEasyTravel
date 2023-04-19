/**
 *
 */
package com.dynatrace.easytravel.launcher.engine;

/**
 * @author tomasz.wieremjewicz
 * @date 12 gru 2019
 *
 * This callback interface is used to inform application code that a dialog was closed.
 *
 * This is a copy from the RWT source code class DialogCallback in order to not have RWT dependencies in
 * pure SWT Launcher.
 *
 */
public interface CloseCallback {

  /**
   * This method is called after a dialog was closed. The meaning of the <code>returnCode</code>
   * is defined by the respective <code>Dialog</code> implementation but usually indicates how the
   * dialog was left. For example, pressing the 'OK' button would lead to the
   * <code>returnCode</code> {@link org.eclipse.swt.SWT#OK SWT.OK}.
   *
   * @param returnCode {@link org.eclipse.swt.SWT#CANCEL SWT.CANCEL} if the dialog was closed with
   *   the shells' close button, a dialog-specific return code otherwise.
   * @see org.eclipse.swt.SWT SWT
   * @see org.eclipse.swt.widgets.Dialog Dialog
   */
  void dialogClosed( int returnCode );
}
