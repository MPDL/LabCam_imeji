package de.mpg.mpdl.labcam.code.common.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import de.mpg.mpdl.labcam.R;


public class PhotoDialog extends Dialog {

    public PhotoDialog(Context context) {
        super(context, R.style.custom_dialog);
        Window window = getWindow(); // 得到对话框
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.setWindowAnimations(R.style.AnimBottom); // 设置窗口弹出动画
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.gravity = Gravity.BOTTOM;
        wl.width = WindowManager.LayoutParams.FILL_PARENT;
        wl.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wl);
    }

    public static class Builder {
        private Context context;
        private String PhotoAlbumButtonText;
        private String cameraButtonText;
        private String cancelButtonText;
        private View contentView;
        private SpannableStringBuilder builder;
        private OnClickListener photoAlbumButtonClickListener;
        private OnClickListener cameraButtonClickListener;
        private OnClickListener cancelButtonClickListener;
        private PhotoDialog dialog;

        public Builder(Context context) {
            this.context = context;
        }

        public boolean isDialogShowing() {
            return dialog.isShowing();
        }


        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }


        public Builder setCancelButton(int cancelButtonText,
                                       OnClickListener listener) {
            this.cancelButtonText = (String) context.getText(cancelButtonText);
            this.cancelButtonClickListener = listener;
            return this;
        }

        public Builder setCancelButton(String cancelButtonText,
                                       OnClickListener listener) {
            this.cancelButtonText = cancelButtonText;
            this.cancelButtonClickListener = listener;
            return this;
        }

        public Builder setPhotoAlbumButton(int positiveButtonText,
                                           OnClickListener listener) {
            this.PhotoAlbumButtonText = (String) context
                    .getText(positiveButtonText);
            this.photoAlbumButtonClickListener = listener;
            return this;
        }

        public Builder setPhotoAlbumButton(String PhotoAlbumButtonText,
                                           OnClickListener listener) {
            this.PhotoAlbumButtonText = PhotoAlbumButtonText;
            this.photoAlbumButtonClickListener = listener;
            return this;
        }

        public Builder setCameraButton(int PhotoAlbumButtonText,
                                       OnClickListener listener) {
            this.cameraButtonText = (String) context
                    .getText(PhotoAlbumButtonText);
            this.cameraButtonClickListener = listener;
            return this;
        }

        public Builder setCameraButton(String cameraButtonText,
                                       OnClickListener listener) {
            this.cameraButtonText = cameraButtonText;
            this.cameraButtonClickListener = listener;
            return this;
        }

        public void cancelDialog() {
            if (dialog != null) {
                dialog.dismiss();
            }
        }

        public PhotoDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            dialog = new PhotoDialog(context);
            dialog.setCanceledOnTouchOutside(true);
            View layout = inflater.inflate(R.layout.photo_dialog_layout, null);

            if (cancelButtonText != null) {
                ((Button) layout.findViewById(R.id.cancelButton))
                        .setText(cancelButtonText);
                if (cancelButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.cancelButton))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    cancelButtonClickListener.onClick(dialog,
                                            DialogInterface.BUTTON_POSITIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.cancelButton).setVisibility(View.GONE);
            }

            if (PhotoAlbumButtonText != null) {
                ((Button) layout.findViewById(R.id.positiveButton))
                        .setText(PhotoAlbumButtonText);
                if (photoAlbumButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.positiveButton))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    photoAlbumButtonClickListener.onClick(dialog,
                                            DialogInterface.BUTTON_POSITIVE);
                                }
                            });
                }
            } else {
                layout.findViewById(R.id.line_two).setVisibility(View.GONE);
                layout.findViewById(R.id.positiveButton).setVisibility(
                        View.GONE);
            }
            // set the cancel button
            if (cameraButtonText != null) {
                ((Button) layout.findViewById(R.id.negativeButton))
                        .setText(cameraButtonText);
                if (cameraButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.negativeButton))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    cameraButtonClickListener.onClick(dialog,
                                            DialogInterface.BUTTON_NEGATIVE);
                                }
                            });
                }
            }
            dialog.setContentView(layout);
            return dialog;
        }

    }
}
