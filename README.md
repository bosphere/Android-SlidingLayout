# Android-SlidingLayout
A flexible layout that slides out children views smoothly from right-most to left-most of the screen to achieve a "bullet curtain" effect

### With `SlidingLayout`, you can:
+ use any type of child `View`
+ configure `velocity`
+ adjust `divider size`

<img src="./arts/screenshot1.png" width="500">
<img src="./arts/screenshot2.png" width="500">

### Usage:
Simply invoke `SlidingLayout.enqueue(SlideItem)` and supply a `SlideItem`. `SlideItem.onCreateView()` will be called only when the item is about to be displayed. Off-screen items are queued in forms of `SlideItem` to avoid measure/layout overhead.

```java
mSlidingLayout.enqueue(new SlidingLayout.SlideItem() {
    @Override
    protected View onCreateView() {
        TextView tv = new TextView(MainActivity.this);
        SpannableStringBuilder ssb = new SpannableStringBuilder("text" + mRnd.nextLong());
        ssb.setSpan(new ImageSpan(MainActivity.this, android.R.drawable.presence_audio_away), ssb.length() - 1,
                ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ssb);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(10 + mRnd.nextInt(10));
        return tv;
    }
});
```
