package com.piceasoft.thesis_project;

import android.annotation.SuppressLint;

class Classification {
    private String confidence;
    private String digit;

    @SuppressLint("DefaultLocale")
    Classification(float[] confidences) {
        int index = getMaxIndex(confidences);

        confidence = String.format("%.3f", confidences[index]);
        digit = String.valueOf(index);
    }

    String getConfidence() {
        return confidence;
    }

    String getDigit() {
        return digit;
    }

    private static int getMaxIndex(float[] confidences) {
        if(confidences != null && confidences.length > 0) {
            int index = 0;

            for(int i = 1; i < confidences.length; i++) {
                if(confidences[index] < confidences[i]) {
                    index = i;
                }
            }

            return index;
        }

        throw new IllegalArgumentException("Confidence array is incorrectly constructed");
    }
}
