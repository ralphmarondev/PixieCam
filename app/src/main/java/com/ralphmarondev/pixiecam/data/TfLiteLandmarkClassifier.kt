package com.ralphmarondev.pixiecam.data

import android.content.Context
import android.graphics.Bitmap
import android.view.Surface
import com.ralphmarondev.pixiecam.domain.Classification
import com.ralphmarondev.pixiecam.domain.LandmarkClassifier
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

class TfLiteLandmarkClassifier(
    private val context: Context,
    private val threshold: Float = 0.5f,
    private val maxResult: Int = 1
) : LandmarkClassifier {

    private var classifier: ImageClassifier? = null

    private fun setupClassifier() {
        val baseOption = BaseOptions.builder()
            .setNumThreads(2)
            .build()

        val option = ImageClassifier
            .ImageClassifierOptions.builder()
            .setBaseOptions(baseOption)
            .setMaxResults(maxResult)
            .setScoreThreshold(threshold)
            .build()

        try {
            classifier = ImageClassifier.createFromFileAndOptions(
                context,
                "landmarks.tflite",
                option
            )
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun classify(bitmap: Bitmap, rotation: Int): List<Classification> {
        if (classifier == null) {
            setupClassifier()
        }

        val imageProcessor = ImageProcessor.Builder().build()
        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))

        val imageProcessingOptions = ImageProcessingOptions.builder()
            .setOrientation(getOrientationFromRotation(rotation))
            .build()

        val results = classifier?.classify(tensorImage, imageProcessingOptions)

        return results?.flatMap { classifications ->
            classifications.categories.map { category ->
                Classification(
                    name = category.displayName,
                    score = category.score
                )
            }
        }?.distinctBy { it.name } ?: emptyList()
    }

    private fun getOrientationFromRotation(rotation: Int): ImageProcessingOptions.Orientation {
        return when (rotation) {
            Surface.ROTATION_0 -> ImageProcessingOptions.Orientation.RIGHT_TOP
            Surface.ROTATION_90 -> ImageProcessingOptions.Orientation.TOP_LEFT
            Surface.ROTATION_180 -> ImageProcessingOptions.Orientation.RIGHT_BOTTOM
            else -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
        }
    }
}