package com.universidad.reta2.domain.services

import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.models.QuestionOption

object QuestionRandomizer{
    fun randomizeQuestion(question: Question): Question{
        val shuffledOptions=question.options.shuffled()

        return question.copy(
            options = shuffledOptions
        )
    }

    fun randomizeQuestions(questions: List<Question>): List<Question>{
        return questions.map{randomizeQuestion(it)}
    }

    fun getDisplayOptions(question: Question): List<Pair<Int, String>>{
        return question.options.map { option ->
            Pair(option.id, option.text)
        }
    }

    fun isCorrectAnswer(question: Question, selectedOptionId: Int): Boolean{
        return selectedOptionId==question.correctOptionId
    }

    fun getCorrectOptionText(question: Question): String{
        return question.options.find{it.id == question.correctOptionId}?.text?:""
    }
}