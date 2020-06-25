package com.piceasoft.thesis_project;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class DigitClassifier {
    private static final int MAX_THREADS = 4;
    private static final String MODEL_FILE = "mnist_model.tflite";
    private static final Interpreter.Options mInterpreterOptions;

    private static final int FLOAT_SIZE = 4;
    private static final int PIXEL_SIZE = 1;
    private static final int INPUT_WIDTH = 28;
    private static final int INPUT_HEIGHT = 28;
    private static final int OUTPUT_CLASSES = 10;

    private final ExecutorService mExecutorService = Executors.newCachedThreadPool();
    private Interpreter mTfLiteInterpreter;

    DigitClassifier(Context context) {
        asyncInit(context);
    }

    Task<Classification> asyncClassify(Bitmap bitmap) {
        return Tasks.call(mExecutorService, () -> classify(bitmap));
    }

    private void asyncInit(Context context) {
        Tasks.call(mExecutorService, () -> {
            try {
                MappedByteBuffer modelData = loadModel(context.getAssets());
                mTfLiteInterpreter = new Interpreter(modelData, mInterpreterOptions);
            } catch(IOException e) {
                throw new IllegalStateException(e);
            }

            return null;
        });
    }

    private Classification classify(Bitmap bitmap) {
        if(mTfLiteInterpreter == null) {
            throw new IllegalStateException("Interpreter is not initialized");
        }

        ByteBuffer byteBuffer = createScaledBitmapBuffer(bitmap);

        final float[][] outputData = new float[1][OUTPUT_CLASSES];
        mTfLiteInterpreter.run(byteBuffer, outputData);

        return new Classification(outputData[0]);
    }

    private ByteBuffer createScaledBitmapBuffer(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_WIDTH, INPUT_HEIGHT, true);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(FLOAT_SIZE * INPUT_WIDTH * INPUT_HEIGHT * PIXEL_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());

        final int[] pixels = new int[INPUT_WIDTH * INPUT_HEIGHT];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for(int pixel : pixels) {
            byteBuffer.putFloat(convertPixel(pixel));
        }

        return byteBuffer;
    }

    private static MappedByteBuffer loadModel(AssetManager assetManager) throws IOException {
        AssetFileDescriptor descriptor = assetManager.openFd(MODEL_FILE);

        FileInputStream inputStream = new FileInputStream(descriptor.getFileDescriptor());
        FileChannel channelData = inputStream.getChannel();

        return channelData.map(FileChannel.MapMode.READ_ONLY, descriptor.getStartOffset(), descriptor.getDeclaredLength());
    }

    private static float convertPixel(int color) {
        return (255 - (((color >> 16) & 0xFF) * 0.299f
                + ((color >> 8) & 0xFF) * 0.587f
                + (color & 0xFF) * 0.114f)) / 255f;
    }

    static {
        mInterpreterOptions = new Interpreter.Options();
        mInterpreterOptions.setNumThreads(MAX_THREADS);
        mInterpreterOptions.setUseNNAPI(false);
    }
}
