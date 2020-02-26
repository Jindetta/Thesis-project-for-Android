package com.piceasoft.thesis_project;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Size;

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

public class DigitClassifier {
    private static final int MAX_THREADS = 4;
    private static final String MODEL_FILE = "digit.tfmodel";
    private static final Interpreter.Options mInterpreterOptions;

    private static final int OUTPUT_CLASSES = 10;
    private static final int FLOAT_SIZE = 4;
    private static final int PIXEL_SIZE = 1;

    private final ExecutorService mExecutorService = Executors.newCachedThreadPool();
    private Interpreter mTfLiteInterpreter;
    private Size mInputImageSize;

    public DigitClassifier(Context context) {
        try {
            MappedByteBuffer modelData = loadModel(context.getAssets());
            Interpreter interpreter = new Interpreter(modelData, mInterpreterOptions);

            int[] shapeData = interpreter.getInputTensor(0).shape();
            mInputImageSize = new Size(shapeData[0], shapeData[1]);
            mTfLiteInterpreter = interpreter;
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Task<Classification> asyncClassify(Bitmap bitmap) {
        return Tasks.call(mExecutorService, () -> classify(bitmap));
    }

    public void close() {
        mTfLiteInterpreter.close();
        mTfLiteInterpreter = null;
    }

    private Classification classify(Bitmap bitmap) {
        if(mTfLiteInterpreter == null) {
            throw new IllegalStateException("Interpreter is not initialized");
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, mInputImageSize.getWidth(), mInputImageSize.getHeight(), true);
        ByteBuffer byteBuffer = bitmapToBuffer(scaledBitmap);

        final float[][] outputData = new float[1][OUTPUT_CLASSES];
        mTfLiteInterpreter.run(byteBuffer, outputData);

        return new Classification(outputData[0]);
    }

    private ByteBuffer bitmapToBuffer(Bitmap bitmap) {
        final int inputSize = mInputImageSize.getWidth() * mInputImageSize.getHeight();

        ByteBuffer byteBuffer = ByteBuffer.allocate(FLOAT_SIZE * inputSize * PIXEL_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());

        final int[] pixels = new int[inputSize];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for(int pixel : pixels) {
            int r = pixel >> 16 & 0xFF;
            int g = pixel >> 8 & 0xFF;
            int b = pixel & 0xFF;

            byteBuffer.putFloat((r + g + b) / 3f / 255f);
        }

        return byteBuffer;
    }

    private static MappedByteBuffer loadModel(AssetManager assetManager) throws IOException {
        AssetFileDescriptor descriptor = assetManager.openFd(MODEL_FILE);

        FileInputStream inputStream = new FileInputStream(descriptor.getFileDescriptor());
        FileChannel channelData = inputStream.getChannel();

        return channelData.map(FileChannel.MapMode.READ_ONLY, descriptor.getStartOffset(), descriptor.getDeclaredLength());
    }

    static {
        mInterpreterOptions = new Interpreter.Options();
        mInterpreterOptions.setNumThreads(MAX_THREADS);
        mInterpreterOptions.setUseNNAPI(true);
    }
}
