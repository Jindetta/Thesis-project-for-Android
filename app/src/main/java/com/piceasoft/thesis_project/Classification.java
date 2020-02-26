package com.piceasoft.thesis_project;

public class Classification {
    private float confidence;
    private int digit;

    Classification(float[] confidences) {
        int index = getMaxIndex(confidences);

        confidence = confidences[index];
        digit = index + 1;
    }

    public float getConfidence() {
        return confidence;
    }

    public int getDigit() {
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
