package com.example.calculator

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.calculator.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import java.text.DecimalFormat

private lateinit var binding: ActivityMainBinding
                                         //Nên dùng cái này để xử lí sự kiện bấm cho nhiều nút cái View...
class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //gán chức năng bấm cho moi nút
        listOf(
            binding.buttonClearAll,
            binding.buttonClear,
            binding.buttonBack,
            binding.buttonChia,
            binding.buttonNhan,
            binding.buttonCong,
            binding.buttonTru,
            binding.buttonBang,
            binding.button0,
            binding.button1,
            binding.button2,
            binding.button3,
            binding.button4,
            binding.button5,
            binding.button6,
            binding.button7,
            binding.button8,
            binding.button9,
            binding.buttonDau,
            binding.buttonCham
        ).forEach { it.setOnClickListener(this) }

    }
     //Xử lí sự kiện bấm
    override fun onClick(view: View?) {
        val button = view as MaterialButton
        val buttonText = button.text.toString()
        var dataToCalculate = binding.textFormula.text.toString()

        when (buttonText) {
            //Xoa so vua dien
            "CE" -> {
                if (dataToCalculate.isNotEmpty()) {
                    if (dataToCalculate.isDigitsOnly()) {
                        binding.textResult.text = "0"
                        binding.textFormula.text = ""
                        return
                    } else {
                        val lastOperatorIndex =
                            dataToCalculate.lastIndexOfAny(charArrayOf('+', '-', 'x', '/'))

                        if (lastOperatorIndex != -1 && lastOperatorIndex != dataToCalculate.length - 1) {
                            dataToCalculate = dataToCalculate.substring(0, lastOperatorIndex + 1)
                        } else {
                            val lastOperatorIndex2 =
                                dataToCalculate.substring(0, lastOperatorIndex - 1)
                                    .lastIndexOfAny(charArrayOf('+', '-', 'x', '/'))
                            dataToCalculate = dataToCalculate.substring(0, lastOperatorIndex2 + 1)
                        }
                    }
                } else {
                    binding.textResult.text = "0"
                    binding.textFormula.text = ""
                    return
                }
            }

            //Clear all
            "C" -> {
                binding.textResult.text = "0"
                binding.textFormula.text = ""
                return
            }

            //Xóa từng kí tự
            "BS" -> {
                if (dataToCalculate.isNotEmpty()) {
                    if (dataToCalculate.length == 1) {
                        binding.textResult.text = "0"
                        binding.textFormula.text = ""
                        return
                    } else {
                        dataToCalculate = dataToCalculate.substring(0, dataToCalculate.length - 1)
                    }
                } else {
                    binding.textResult.text = "0"
                    binding.textFormula.text = ""
                    return
                }
            }

            //Thay kêt quả lên TextView phép tính
            "=" -> {
                binding.textFormula.setText(binding.textResult.text.toString())
                return
            }

            //Đảo dấu số mới nhất vừa điền
            "+/-" -> {
                if (dataToCalculate.isNotEmpty()) {
                    if (dataToCalculate.toIntOrNull() != null) {
                        val temp = dataToCalculate.toInt() * -1
                        dataToCalculate = temp.toString()
                    } else if (dataToCalculate.toFloatOrNull() != null) {
                        val temp = dataToCalculate.toFloat() * -1
                        dataToCalculate = temp.toString()
                    } else {
                        val temp = dataToCalculate.lastIndexOfAny(charArrayOf('+', '-', '/', 'x'))
                        if (dataToCalculate[temp] == '-') {
                            if (dataToCalculate[temp - 1].isDigit()) {
                                val temp1 = dataToCalculate.substring(0, temp)
                                val temp2 = dataToCalculate.substring(temp + 1)
                                dataToCalculate = temp1 + "+" + temp2
                            } else {
                                val temp1 = dataToCalculate.substring(0, temp)
                                val temp2 = dataToCalculate.substring(temp + 1)
                                dataToCalculate = temp1 + temp2
                            }
                        } else {
                            val temp1 = dataToCalculate.substring(0, temp + 1)
                            val temp2 = dataToCalculate.substring(temp + 1)
                            dataToCalculate = temp1 + "-" + temp2
                        }
                    }
                } else {
                    binding.textResult.text = "0"
                    binding.textFormula.text = ""
                    return
                }
            }

            //viết thành phép tính dài ở trên cùng
            else -> {
                dataToCalculate = dataToCalculate + buttonText
            }
        }

        binding.textFormula.setText(dataToCalculate)

        val finalResult = getResult(dataToCalculate)
        if (finalResult != "Err") {
            binding.textResult.text = finalResult
        }
    }

    // Dùng thu vien de thuc hien chuc nang tinh toan
    fun getResult(data: String): String {
        return try {
            val context = Context.enter()
            context.optimizationLevel = -1
            val scriptable: Scriptable = context.initStandardObjects()

            val formattedData = data.replace("x", "*")

            val rawResult =
                context.evaluateString(scriptable, formattedData, "JavaScript", 1, null).toString()


            val number = rawResult.toDoubleOrNull() ?: return "Err"


            //neu ra so vo ty thì chỉ lay 6 so sau dau phay
            val decimalFormat = DecimalFormat("#.######")
            var finalResult = decimalFormat.format(number)


            if (finalResult.endsWith(".0")) {
                finalResult = finalResult.replace(".0", "")
            }

            finalResult
        } catch (e: Exception) {
            "Err"
        } finally {
            Context.exit()
        }
    }

}