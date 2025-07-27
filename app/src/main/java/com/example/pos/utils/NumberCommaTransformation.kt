package com.example.pos.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.*
import kotlin.math.max

class NumberCommaTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val number = originalText.toLongOrNull()
        if (number == null) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formatter = NumberFormat.getNumberInstance(Locale.JAPAN)
        val formattedText = formatter.format(number)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val textBeforeCursor = originalText.substring(0, offset)
                // 👇 このif文を追加して、空の文字列を変換しようとするのを防ぐ
                val commasBeforeCursor = if (textBeforeCursor.isNotEmpty()) {
                    formatter.format(textBeforeCursor.toLong()).count { it == ',' }
                } else {
                    0
                }
                return offset + commasBeforeCursor
            }

            override fun transformedToOriginal(offset: Int): Int {
                val textBeforeCursor = formattedText.substring(0, offset)
                val commasBeforeCursor = textBeforeCursor.count { it == ',' }
                return max(0, offset - commasBeforeCursor)
            }
        }

        return TransformedText(
            text = AnnotatedString(formattedText),
            offsetMapping = offsetMapping
        )
    }
}