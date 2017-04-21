package com.jess.arms.common.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.jess.arms.common.assist.Check;
import com.jess.arms.common.io.IOUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by lujianzhao on 16/3/31.
 */
public class ImageUtils {

    /**
     * 调整照片的色调,饱和度,亮度
     * MID_VALUE = 177; // 进度条的中间值
     *
     * @param bm         原图
     * @param hue        色调 = (进度条进度 - MID_VALUE) * 1.0F / MIN_VALUE * 180
     * @param saturation 饱和度 = 进度条进度 * 1.0F / MIN_VALUE;
     * @param lum        亮度 = 进度条进度 * 1.0F / MIN_VALUE;
     * @return 调整后的图片
     */
    public static Bitmap handleImageEffect(Bitmap bm, int hue, int saturation, int lum) {
        Bitmap bitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        ColorMatrix hueMatrix = new ColorMatrix();
        hueMatrix.setRotate(0, hue);
        hueMatrix.setRotate(1, hue);
        hueMatrix.setRotate(2, hue);

        ColorMatrix saturationMatrix = new ColorMatrix();
        saturationMatrix.setSaturation(saturation);

        ColorMatrix lumMatrix = new ColorMatrix();
        lumMatrix.setScale(lum, lum, lum, 1);

        ColorMatrix imageMatrix = new ColorMatrix();
        imageMatrix.postConcat(hueMatrix);
        imageMatrix.postConcat(saturationMatrix);
        imageMatrix.postConcat(lumMatrix);

        paint.setColorFilter(new ColorMatrixColorFilter(imageMatrix));
        canvas.drawBitmap(bm, 0, 0, paint);
        return bitmap;
    }

    public static Observable<File> downLoadImageWithGlide(final Activity activity, final String url) {

        final Context context = activity.getApplication();

        return new RxPermissions(activity).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .flatMap(new Function<Boolean, ObservableSource<File>>() {
            @Override
            public ObservableSource<File> apply(Boolean granted) throws Exception {
                File file = Glide.with(context).load(url).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                String desPath;
                if (granted) {
                    desPath = getExternalStoragePath(context);
                } else {
                    desPath = FileUtil.getIconDir(context);
                }
                file = savePhotoToSDCard(context, file.getAbsolutePath(),desPath);

                return Observable.just(file);
            }
        }).observeOn(AndroidSchedulers.mainThread());



//        return Observable.create(new ObservableOnSubscribe<File>() {
//            @Override
//            public void subscribe(@NonNull ObservableEmitter<File> emitter) throws Exception {
//                try {
//                    File file = Glide.with(context).load(url).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
//                    // 在这里执行图片保存方法
//                    file = savePhotoToSDCard(context, file);
//                    emitter.onNext(file);
//                    emitter.onComplete();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    emitter.onError(e);
//                }
//            }
//
//        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private static String getExternalStoragePath(Context context) {
        String dir = FileUtil.getExternalStorageDirectory(context.getPackageName());
        if (Check.isEmpty(dir)) {
            return null;
        }
        StringBuilder sb = new StringBuilder(dir);
        sb.append(File.separator);
        sb.append(Environment.DIRECTORY_PICTURES);
        return sb.toString();
    }

    /**
     * 保存到SD卡
     *
     * @param context
     * @param path
     * @param desPath
     */
    private static File savePhotoToSDCard(Context context,String path, String desPath) throws IOException {
        File dir = new File(desPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File currentFile = new File(dir.getAbsolutePath(), fileName);


        ByteArrayOutputStream baos = null;
        FileOutputStream fOut = null;
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, opts);
            opts.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(path, opts);
            baos = new ByteArrayOutputStream();
            int options = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);

            fOut = new FileOutputStream(currentFile);
            fOut.write(baos.toByteArray());
            fOut.flush();
            baos.flush();
        } finally {
            IOUtils.closeQuietly(fOut);
            fOut = null;
            IOUtils.closeQuietly(baos);
            baos = null;

            if (bitmap != null && !bitmap.isRecycled()) {
                // 回收并且置为null
                bitmap.recycle();
                bitmap = null;
            }
        }

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(currentFile)));
        return currentFile;
    }
}
