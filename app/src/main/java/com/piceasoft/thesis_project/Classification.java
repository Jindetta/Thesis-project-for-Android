package com.piceasoft.thesis_project;

class Classification {
    private String confidence;
    private String digit;

    Classification(float[] confidences) {
        int index = getMaxIndex(confidences);

        confidence = String.valueOf(confidences[index]);
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
