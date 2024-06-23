package com.yeloe.attentanceapp.utils

import android.util.Log
import com.yeloe.attentanceapp.DB.DBHelper
import com.yeloe.attentanceapp.ui.face_classification.FaceClassifier.Recognition

class EmBeddingConverter {

    companion object {
        fun getStringFromEmbedding(embedding: Recognition): String {
            val embeddingData = embedding.embeeding
            val floatList = embeddingData as Array<FloatArray>
            var embeddingString = ""
            for (f in floatList[0]) {
                embeddingString += "$f,"
            }
            return embeddingString
        }

        fun getEmbeddingFromString(embedding: String, id: String): Recognition {
            Log.d(Constant.REPOSITORY_LOG,"Get $embedding")
            val stringList: Array<String> =
                embedding.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val embeddingFloat = ArrayList<Float>()
            for (s in stringList) {
                embeddingFloat.add(s.toFloat())
            }
            val bigArray = Array(1) {
                FloatArray(
                    1
                )
            }
            val floatArray = FloatArray(embeddingFloat.size)
            for (i in embeddingFloat.indices) {
                floatArray[i] = embeddingFloat[i]
            }
            bigArray[0] = floatArray
            embeddingFloat.removeAt(embeddingFloat.size - 1)
            return Recognition(id, bigArray)
        }

//        fun ji (){
//            val embeddingString: String =
//                res.getString(res.getColumnIndex(DBHelper.FACE_COLUMN_EMBEDDING))
//            val stringList = embeddingString.split(",".toRegex()).dropLastWhile { it.isEmpty() }
//                .toTypedArray()
//            val embeddingFloat = java.util.ArrayList<Float>()
//            for (s in stringList) {
//                embeddingFloat.add(s.toFloat())
//            }
//            val bigArray = Array(1) {
//                FloatArray(
//                    1
//                )
//            }
//            val floatArray = FloatArray(embeddingFloat.size)
//            for (i in embeddingFloat.indices) {
//                floatArray[i] = embeddingFloat[i]
//            }
//            bigArray[0] = floatArray
//            embeddingFloat.removeAt(embeddingFloat.size - 1)
//            val recognition =
//                Recognition(res.getString(res.getColumnIndex(DBHelper.FACE_COLUMN_NAME)), bigArray)
//        }
//

    }
}