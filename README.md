# Android 点击 AlertDialog 的确定按钮对话框不关闭的方法
## 一、问题描述
> 最近在做项目需要在弹出 含 EditText 让用户输入的对话框，在按确定按钮时需要对输入的内容进行校验，但出现的问题是一按确定按钮就对话框就关闭，现在的问题是要让对话框在按确定按钮时对 EditText 的内容进行校验，如果校验不通过就不关闭，校验通过才关闭。

```java
private AppCompatButton mBtnShowDialog;
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mBtnShowDialog = (AppCompatButton)findViewById(R.id.btn_show_dialog);
    View viewDialog = getLayoutInflater().inflate(R.layout.view_dialog, null);
    final AppCompatEditText etInput = (AppCompatEditText)viewDialog.findViewById(R.id.et_input);
    final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
            .setTitle("我是标题啊")
            .setView(viewDialog)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String text = etInput.getText().toString().trim();
                    if (!TextUtils.isEmpty(text)) {
                        Toast.makeText(MainActivity.this,
                                "" + text, Toast.LENGTH_SHORT).show();
                    } else {
                            Toast.makeText(MainActivity.this, "输入为空", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).create();
    mBtnShowDialog.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            dialog.show();
        }
    });
}
```

> 按照上面的这样做是没办法阻止 Dialog 关闭的

![image](https://github.com/cekiasoo/AlertDialogTest/raw/master/screenshots/1.gif)<br/>

## 二、源码分析
> 于是就去看了下源码，
先点进 AlertDialog.Builder 的 setPositiveButton 方法去看看，

![image](https://github.com/cekiasoo/AlertDialogTest/raw/master/screenshots/1.png)<br/>

> 可以看到传进去的参数是赋给一个叫 P 的引用的

```java
/**
         * Set a listener to be invoked when the positive button of the dialog is pressed.
         * @param textId The resource id of the text to display in the positive button
         * @param listener The {@link DialogInterface.OnClickListener} to use.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setPositiveButton(@StringRes int textId, final OnClickListener listener) {
            P.mPositiveButtonText = P.mContext.getText(textId);
            P.mPositiveButtonListener = listener;
            return this;
        }
```

> 这个 P 的引用呢，是 AlertController.AlertParams 类型的

```java
    public static class Builder {
        private final AlertController.AlertParams P;
        private final int mTheme;

        /**
         * Creates a builder for an alert dialog that uses the default alert
         * dialog theme.
         * <p>
         * The default alert dialog theme is defined by
         * {@link android.R.attr#alertDialogTheme} within the parent
         * {@code context}'s theme.
         *
         * @param context the parent context
         */
        public Builder(@NonNull Context context) {
            this(context, resolveDialogTheme(context, 0));
        }
        ......
    }
```
> 再来看看 AlertDialog.Builder 的 create 方法

![image](https://github.com/cekiasoo/AlertDialogTest/raw/master/screenshots/2.png)<br/>

> 点击进去，

```java
/**
         * Creates an {@link AlertDialog} with the arguments supplied to this
         * builder.
         * <p>
         * Calling this method does not display the dialog. If no additional
         * processing is needed, {@link #show()} may be called instead to both
         * create and display the dialog.
         */
        public AlertDialog create() {
            // We can't use Dialog's 3-arg constructor with the createThemeContextWrapper param,
            // so we always have to re-set the theme
            final AlertDialog dialog = new AlertDialog(P.mContext, mTheme);
            P.apply(dialog.mAlert);
            dialog.setCancelable(P.mCancelable);
            if (P.mCancelable) {
                dialog.setCanceledOnTouchOutside(true);
            }
            dialog.setOnCancelListener(P.mOnCancelListener);
            dialog.setOnDismissListener(P.mOnDismissListener);
            if (P.mOnKeyListener != null) {
                dialog.setOnKeyListener(P.mOnKeyListener);
            }
            return dialog;
        }
```

> 可以看到这里有个 P.apply(dialog.mAlert); 再进去这里边看看，

```java
        public void apply(AlertController dialog) {
            if (mCustomTitleView != null) {
                dialog.setCustomTitle(mCustomTitleView);
            } else {
                if (mTitle != null) {
                    dialog.setTitle(mTitle);
                }
                if (mIcon != null) {
                    dialog.setIcon(mIcon);
                }
                if (mIconId != 0) {
                    dialog.setIcon(mIconId);
                }
                if (mIconAttrId != 0) {
                    dialog.setIcon(dialog.getIconAttributeResId(mIconAttrId));
                }
            }
            if (mMessage != null) {
                dialog.setMessage(mMessage);
            }
            if (mPositiveButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, mPositiveButtonText,
                        mPositiveButtonListener, null);
            }
            if (mNegativeButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, mNegativeButtonText,
                        mNegativeButtonListener, null);
            }
            ......
        }
```

> 这是在 AlertController.AlertParams 类里的，有个 dialog.setButton , 传入 text 和 listener 等，那就再点进去看看吧

```java
/**
 * Sets a click listener or a message to be sent when the button is clicked.
 * You only need to pass one of {@code listener} or {@code msg}.
 *
 * @param whichButton Which button, can be one of
 *                    {@link DialogInterface#BUTTON_POSITIVE},
 *                    {@link DialogInterface#BUTTON_NEGATIVE}, or
 *                    {@link DialogInterface#BUTTON_NEUTRAL}
 * @param text        The text to display in positive button.
 * @param listener    The {@link DialogInterface.OnClickListener} to use.
 * @param msg         The {@link Message} to be sent when clicked.
 */
public void setButton(int whichButton, CharSequence text,
        DialogInterface.OnClickListener listener, Message msg) {

    if (msg == null && listener != null) {
        msg = mHandler.obtainMessage(whichButton, listener);
    }

    switch (whichButton) {

        case DialogInterface.BUTTON_POSITIVE:
            mButtonPositiveText = text;
            mButtonPositiveMessage = msg;
            break;

        case DialogInterface.BUTTON_NEGATIVE:
            mButtonNegativeText = text;
            mButtonNegativeMessage = msg;
            break;

        case DialogInterface.BUTTON_NEUTRAL:
            mButtonNeutralText = text;
            mButtonNeutralMessage = msg;
            break;

        default:
            throw new IllegalArgumentException("Button does not exist");
    }
}
```

> 看到有个 mHandler.obtainMessage 和 和 mButtonPositiveMessage，mButtonNegativeMessage 等，原来是用的 Handler， 那就看一下这个 mHandler，

```java
public AlertController(Context context, AppCompatDialog di, Window window) {
    mContext = context;
    mDialog = di;
    mWindow = window;
    mHandler = new ButtonHandler(di);
}
```

> 这个 mHandler 是 ButtonHandler 那就去看看 ButtonHandler

```java
private final View.OnClickListener mButtonHandler = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        final Message m;
        if (v == mButtonPositive && mButtonPositiveMessage != null) {
            m = Message.obtain(mButtonPositiveMessage);
        } else if (v == mButtonNegative && mButtonNegativeMessage != null) {
            m = Message.obtain(mButtonNegativeMessage);
        } else if (v == mButtonNeutral && mButtonNeutralMessage != null) {
            m = Message.obtain(mButtonNeutralMessage);
        } else {
            m = null;
        }

        if (m != null) {
            m.sendToTarget();
        }

        // Post a message so we dismiss after the above handlers are executed
        mHandler.obtainMessage(ButtonHandler.MSG_DISMISS_DIALOG, mDialog)
                .sendToTarget();
    }
};

private static final class ButtonHandler extends Handler {
    // Button clicks have Message.what as the BUTTON{1,2,3} constant
    private static final int MSG_DISMISS_DIALOG = 1;

    private WeakReference<DialogInterface> mDialog;

    public ButtonHandler(DialogInterface dialog) {
        mDialog = new WeakReference<>(dialog);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {

            case DialogInterface.BUTTON_POSITIVE:
            case DialogInterface.BUTTON_NEGATIVE:
            case DialogInterface.BUTTON_NEUTRAL:
                ((DialogInterface.OnClickListener) msg.obj).onClick(mDialog.get(), msg.what);
                break;

            case MSG_DISMISS_DIALOG:
                ((DialogInterface) msg.obj).dismiss();
        }
    }
}
```

> 在看 ButtonHandler 的时候发现上面有个 View.OnClickListener 的匿名内部类有一句注释就比较有意思 Post a message so we dismiss after the above handlers are executed，
说在执行上面的操作后就 dismiss，好吧，找找看在哪里用到 mButtonHandler

```java
    private void setupButtons(ViewGroup buttonPanel) {
        int BIT_BUTTON_POSITIVE = 1;
        int BIT_BUTTON_NEGATIVE = 2;
        int BIT_BUTTON_NEUTRAL = 4;
        int whichButtons = 0;
        mButtonPositive = (Button) buttonPanel.findViewById(android.R.id.button1);
        mButtonPositive.setOnClickListener(mButtonHandler);

        if (TextUtils.isEmpty(mButtonPositiveText)) {
            mButtonPositive.setVisibility(View.GONE);
        } else {
            mButtonPositive.setText(mButtonPositiveText);
            mButtonPositive.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_POSITIVE;
        }

        mButtonNegative = (Button) buttonPanel.findViewById(android.R.id.button2);
        mButtonNegative.setOnClickListener(mButtonHandler);

        if (TextUtils.isEmpty(mButtonNegativeText)) {
            mButtonNegative.setVisibility(View.GONE);
        } else {
            mButtonNegative.setText(mButtonNegativeText);
            mButtonNegative.setVisibility(View.VISIBLE);

            whichButtons = whichButtons | BIT_BUTTON_NEGATIVE;
        }

        mButtonNeutral = (Button) buttonPanel.findViewById(android.R.id.button3);
        mButtonNeutral.setOnClickListener(mButtonHandler);
        ......
    }
```

> 可以看到原来在 setupButtons 这个方法中， mButtonHandler 就是 对话框的 Button 的 点击事件，所以点击按钮时后对话框都会关闭，那不想关闭的话该怎么办？也是有办法的。

## 三、问题解决
> 在看源代码的时候看到 AlertDialog 有个 getButton 方法

```java
/**
     * Gets one of the buttons used in the dialog. Returns null if the specified
     * button does not exist or the dialog has not yet been fully created (for
     * example, via {@link #show()} or {@link #create()}).
     *
     * @param whichButton The identifier of the button that should be returned.
     *                    For example, this can be
     *                    {@link DialogInterface#BUTTON_POSITIVE}.
     * @return The button from the dialog, or null if a button does not exist.
     */
    public Button getButton(int whichButton) {
        return mAlert.getButton(whichButton);
    }
```

> 这个方法是 public 的，说明在外面可以调用，参数从注释中可以看出是获取 Button 的标志，里面是 mAlert.getButton(whichButton)，mAlert 是 AlertController 类型的引用，进去看看，

```java
public Button getButton(int whichButton) {
    switch (whichButton) {
        case DialogInterface.BUTTON_POSITIVE:
            return mButtonPositive;
        case DialogInterface.BUTTON_NEGATIVE:
            return mButtonNegative;
        case DialogInterface.BUTTON_NEUTRAL:
            return mButtonNeutral;
        default:
            return null;
    }
}
```

> 原来就是根据 DialogInterface.BUTTON_XXX 返回不同的 Button，而我们刚才在 setupButtons 方法中看过，mButtonPositive 等是通过 findViewById 获得的，所以我们可以获得 Button 后自己再实现 setOnClickListener，这样就可以了，还有一点要注意的就是 调用了 Dialog 的 show 方法才可以使用 getButton ，getButton 的注释说的很清楚，如果指定的 Button 不存在或者 dialog 还没完全创建就会返回 null, 那在哪里调用 getButton 方法呢？Dialog 有个 setOnShowListener 方法

```java
/**
 * Sets a listener to be invoked when the dialog is shown.
 * @param listener The {@link DialogInterface.OnShowListener} to use.
 */
public void setOnShowListener(@Nullable OnShowListener listener) {
    if (listener != null) {
        mShowMessage = mListenersHandler.obtainMessage(SHOW, listener);
    } else {
        mShowMessage = null;
    }
}
```

> 从注释可以看出当 Dialog 显示的时候就会调用，所以就在这里调用 getButton，按照下面这样就可以了实现点击按钮 Dialog 不关闭了

```java
private AppCompatButton mBtnShowDialog;
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mBtnShowDialog = (AppCompatButton)findViewById(R.id.btn_show_dialog);
    View viewDialog = getLayoutInflater().inflate(R.layout.view_dialog, null);
    final AppCompatEditText etInput = (AppCompatEditText)viewDialog.findViewById(R.id.et_input);
    final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
            .setTitle("我是标题啊")
            .setView(viewDialog)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).create();
    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
        @Override
        public void onShow(DialogInterface dialogInterface) {
            Button btnPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            btnPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text = etInput.getText().toString().trim();
                    if (!TextUtils.isEmpty(text)) {
                        Toast.makeText(MainActivity.this,
                                "" + text, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this,
                                "你还真随便", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    });
    mBtnShowDialog.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dialog.show();
        }
    });
}
```

![image](https://github.com/cekiasoo/AlertDialogTest/raw/master/screenshots/2.gif)<br/>
