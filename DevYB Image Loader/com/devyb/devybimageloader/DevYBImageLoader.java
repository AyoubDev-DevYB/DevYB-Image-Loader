package com.devyb.devybimageloader;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.runtime.Image;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

@DesignerComponent(version = 6,  description = "DevYB Image Loader developed by DevYB.\n" +
        "Extension for image loading and caching.",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,   iconName = "https://res.cloudinary.com/dujfnjfcz/image/upload/v1596225104/icon16.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
@UsesLibraries(libraries = "universal-image-loader-1.9.5.jar")
@SimpleObject(external = true)
public class DevYBImageLoader extends AndroidNonvisibleComponent {
    private Context context;
    private ComponentContainer container;
    private ImageLoader imageLoader;
    private boolean cacheOnDisk = true;
    private boolean cacheInMemory = true;
    private DisplayImageOptions displayImageOptions;
    private Drawable imageLoading;
    private Drawable imageError;


    public DevYBImageLoader(ComponentContainer container) {
        super(container.$form());
        container = container;
        context = container.$context();
    }

    @DesignerProperty(editorType = "boolean", defaultValue = "false")
    @SimpleProperty(userVisible = false, description = "")
    public void DisableCacheOnDisk(boolean value) {
        if (value)
            cacheOnDisk = false;
        else
            cacheOnDisk = true;

    }

    @DesignerProperty(editorType = "boolean", defaultValue = "false")
    @SimpleProperty(userVisible = false, description = "")
    public void DisableCacheInMemory(boolean value) {
        if (value)
            cacheInMemory = false;
        else
            cacheInMemory = true;

    }

    @DesignerProperty(editorType = "asset", defaultValue = "")
    @SimpleProperty
    public void LoadingImage(String path) {
        String pathPic = (path == null) ? "" : path;
        try {
            imageLoading = MediaUtil.getBitmapDrawable(container.$form(), pathPic);
        } catch (IOException exception) {
            imageLoading = null;
        }
    }

    @DesignerProperty(editorType = "asset", defaultValue = "")
    @SimpleProperty
    public void ErrorImage(String path){
        String pathPic = (path == null) ? "" : path;
        try {
            imageError = MediaUtil.getBitmapDrawable(container.$form(), pathPic);
        } catch (IOException e){
            imageError = null;
        }
    }


    @SimpleFunction
    public void LoadImage(Image imageComponent, String url) {
        ImageView imageView = (ImageView) imageComponent.getView();
        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context).build();
        imageLoader.init(configuration);

        displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(cacheInMemory)
                .cacheOnDisk(cacheOnDisk)
                .showImageOnLoading(imageLoading)
                .showImageOnFail(imageError)
                .build();
        imageLoader.displayImage(url, imageView, displayImageOptions, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                OnLoadingStarted(s);
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                OnLoadingFailed(s);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                OnLoadingComplete(s);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
            }
        }, new ImageLoadingProgressListener() {
            @Override
            public void onProgressUpdate(String s, View view, int current, int total) {
                OnProgress(s, current, total);
            }
        });
    }

    @SimpleFunction
    public void ClearDiskCache() {
        try {
            imageLoader.clearDiskCache();
        } catch (Exception exception) {}
    }

    @SimpleFunction
    public void ClearMemoryCache() {
        try {
            imageLoader.clearMemoryCache();
        } catch (Exception exception) {}
    }

    @SimpleEvent
    public void OnLoadingStarted(String url) {
        EventDispatcher.dispatchEvent( this, "OnLoadingStarted", url);
    }

    @SimpleEvent
    public void OnLoadingFailed(String url) {
        EventDispatcher.dispatchEvent( this, "OnLoadingFailed",url);
    }

    @SimpleEvent
    public void OnLoadingComplete(String url) {
        EventDispatcher.dispatchEvent(this, "OnLoadingComplete", url);
    }


    @SimpleEvent
    public void OnProgress(String url, int current, int total) {
        EventDispatcher.dispatchEvent(this, "OnProgress", url, current, total);
    }

    @SimpleEvent
    public void OnExtractedCachedImage (String path){
        EventDispatcher.dispatchEvent(this, "OnExtractedCachedImage", path);
    }



    @SimpleFunction
    public void LoadImageAsync(Image imageComponent, String url) {
        ImageView imageView = (ImageView) imageComponent.getView();

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(3)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory().build();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(cacheInMemory)
                .cacheOnDisk(cacheOnDisk)
                .showImageOnLoading(imageLoading)
                .showImageOnFail(imageError)
                .build();
        imageLoader.getInstance().init(configuration);
        imageLoader.getInstance().displayImage(url, imageView, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                OnLoadingStarted(s);
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                OnLoadingFailed(s);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                OnLoadingComplete(s);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
            }
        }, new ImageLoadingProgressListener() {
            @Override
            public void onProgressUpdate(String s, View view, int current, int total) {
                OnProgress(s, current, total);
            }
        });

    }




    @SimpleFunction
    public void LoadImageInArrangement(final HVArrangement hvArrangement, String url) {

        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context).build();
        imageLoader.init(configuration);

        displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(cacheInMemory)
                .cacheOnDisk(cacheOnDisk)
                .build();
        imageLoader.loadImage(url, displayImageOptions, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                OnLoadingStarted(s);
                ViewUtil.setBackgroundImage(((ViewGroup) hvArrangement.getView()).getChildAt(0), imageLoading);
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                OnLoadingFailed(s);
                ViewUtil.setBackgroundImage(((ViewGroup) hvArrangement.getView()).getChildAt(0), imageError);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                OnLoadingComplete(s);
                ViewUtil.setBackgroundDrawable(((ViewGroup) hvArrangement.getView()).getChildAt(0), new BitmapDrawable(bitmap));
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
            }
        });
    }

    @SimpleFunction
    public void LoadImageAsyncInArrangement(final HVArrangement hvArrangement, String url) {

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(3)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory().build();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(cacheInMemory)
                .cacheOnDisk(cacheOnDisk)
                .build();
        imageLoader.getInstance().init(configuration);
        imageLoader.getInstance().loadImage(url, options, new SimpleImageLoadingListener(){
            @Override
            public void onLoadingStarted(String s, View view) {
                OnLoadingStarted(s);
                ViewUtil.setBackgroundImage(((ViewGroup)hvArrangement.getView()).getChildAt(0), imageLoading);
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                OnLoadingFailed(s);
                ViewUtil.setBackgroundImage(((ViewGroup)hvArrangement.getView()).getChildAt(0), imageError);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                OnLoadingComplete(s);
                ViewUtil.setBackgroundDrawable(((ViewGroup)hvArrangement.getView()).getChildAt(0), new BitmapDrawable(bitmap));
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
            }
        });
    }

    @SimpleFunction
    public void PauseLoading() {
        imageLoader.pause();
    }

    @SimpleFunction
    public void ResumeLoading() {
        imageLoader.resume();
    }

    @SimpleFunction
    public void StopLoading() {
        imageLoader.stop();
    }

    @SimpleFunction
    public void DeleteCachedImage(String url) {
        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context).build();
        imageLoader.init(configuration);
        imageLoader.getInstance().getDiskCache().get(url).delete();

    }

    @SimpleFunction
    public Object CachedImageExists(String url) {
        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context).build();
        imageLoader.init(configuration);
        return imageLoader.getInstance().getDiskCache().get(url).exists();
    }



    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    private boolean checkWriteExternalPermission(){
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }



    @SimpleFunction
    public void ExtractCachedImage(String url, String saveIn, String extension) {
        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context).build();
        imageLoader.init(configuration);
        if (imageLoader.getInstance().getDiskCache().get(url).exists()){
            File desImage= new File (saveIn + imageLoader.getInstance().getDiskCache().get(url).getName() + extension);
            try {
                copyFile(imageLoader.getInstance().getDiskCache().get(url), desImage);
                if (desImage.exists())
                    OnExtractedCachedImage(desImage.getPath());


            } catch (IOException exception1) {
            }

        }
    }

    @SimpleFunction
    public void CacheImage( String url) {

        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context).build();
        imageLoader.init(configuration);
        displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(cacheInMemory)
                .cacheOnDisk(cacheOnDisk)
                .build();
        imageLoader.loadImage(url, displayImageOptions, new SimpleImageLoadingListener(){
            @Override
            public void onLoadingStarted(String s, View view) {
                OnLoadingStarted(s);
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                OnLoadingFailed(s);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                OnLoadingComplete(s);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
            }
        });
    }
}
