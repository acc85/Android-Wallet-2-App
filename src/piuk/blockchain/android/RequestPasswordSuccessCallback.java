package piuk.blockchain.android;

import android.view.View;
import android.view.ViewGroup;

import piuk.blockchain.android.ui.dialogs.RequestForgotPasswordDialog;

public interface RequestPasswordSuccessCallback {
	public void onSuccess(RequestForgotPasswordDialog requestForgotPasswordDialog);

	public void onFail(RequestForgotPasswordDialog requestForgotPasswordDialog);
}