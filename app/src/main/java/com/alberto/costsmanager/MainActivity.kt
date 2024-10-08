package com.alberto.costsmanager
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // List to store all expenses
    private val expenseList = mutableListOf<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to your updated layout
        setContentView(R.layout.activity_main)

        // Initialize the total expenses display
        updateTotal()

        // Set up the "Add Expense" button
        val addExpenseButton = findViewById<Button>(R.id.addExpenseButton)
        addExpenseButton.setOnClickListener {
            showAddExpenseDialog()
        }

        // Set up the income input field
        val incomeEditText = findViewById<EditText>(R.id.incomeEditText)
        incomeEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        // Optionally, initialize with some expenses (for testing purposes)
        // val initialExpense = Expense("Rent", 50.0, 1500.0, 18000.0)
        // expenseList.add(initialExpense)
        // addRow(initialExpense)
    }

    // Function to show the dialog for adding a new expense
    private fun showAddExpenseDialog() {
        // Inflate the custom dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add Expense")

        // Show the dialog
        val alertDialog = builder.create()
        alertDialog.show()

        // Reference UI elements in the dialog
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val amountEditText = dialogView.findViewById<EditText>(R.id.amountEditText)
        val frequencyRadioGroup = dialogView.findViewById<RadioGroup>(R.id.frequencyRadioGroup)
        val saveButton = dialogView.findViewById<Button>(R.id.saveExpenseButton)

        // Set default frequency selection to "Daily"
        frequencyRadioGroup.check(R.id.dailyRadioButton)

        // Handle the "Save" button click
        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val amountText = amountEditText.text.toString().trim()
            val selectedFrequencyId = frequencyRadioGroup.checkedRadioButtonId

            // Input validation for name
            if (name.isEmpty()) {
                nameEditText.error = "Please enter a name"
                return@setOnClickListener
            }

            // Input validation for amount
            val amount = amountText.toDoubleOrNull()
            if (amount == null) {
                amountEditText.error = "Please enter a valid amount"
                return@setOnClickListener
            }

            // Initialize expense amounts
            var daily = 0.0
            var monthly = 0.0
            var annual = 0.0

            // Calculate other amounts based on selected frequency
            when (selectedFrequencyId) {
                R.id.dailyRadioButton -> {
                    daily = amount
                    monthly = daily * 30 // Approximate average
                    annual = daily * 365
                }
                R.id.monthlyRadioButton -> {
                    monthly = amount
                    daily = monthly / 30
                    annual = monthly * 12
                }
                R.id.annualRadioButton -> {
                    annual = amount
                    monthly = annual / 12
                    daily = annual / 365
                }
                else -> {
                    // Should not happen, but handle the case
                    Toast.makeText(this, "Please select a frequency", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Create new expense and add it to the list and table
            val newExpense = Expense(name, daily, monthly, annual)
            expenseList.add(newExpense)
            addRow(newExpense)

            // Update the total expenses
            updateTotal()

            // Dismiss the dialog
            alertDialog.dismiss()
        }
    }

    // Function to add a new row to the table for an expense
    private fun addRow(expense: Expense) {
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)
        val tableRow = TableRow(this)

        // Create TextViews for each data field
        val nameTextView = TextView(this).apply {
            text = expense.name
        }
        val dailyTextView = TextView(this).apply {
            text = "%.2f".format(expense.daily)
        }
        val monthlyTextView = TextView(this).apply {
            text = "%.2f".format(expense.monthly)
        }
        val annualTextView = TextView(this).apply {
            text = "%.2f".format(expense.annual)
        }

        // Create a Button to hide/show the expense
        val hideButton = Button(this).apply {
            text = if (expense.isHidden) "Show" else "Hide"
            setOnClickListener {
                // Toggle the hidden state
                expense.isHidden = !expense.isHidden
                text = if (expense.isHidden) "Show" else "Hide"
                // Change the background color based on hidden state
                tableRow.setBackgroundColor(
                    if (expense.isHidden) Color.GRAY else Color.TRANSPARENT
                )
                // Update the total expenses
                updateTotal()
            }
        }

        // Create a Button to delete the expense
        val deleteButton = Button(this).apply {
            text = "Delete"
            setOnClickListener {
                // Remove the expense from the list and table
                expenseList.remove(expense)
                tableLayout.removeView(tableRow)
                // Update the total expenses
                updateTotal()
            }
        }

        // Create a Button to edit the expense
        val editButton = Button(this).apply {
            text = "Edit"
            setOnClickListener {
                // Open the edit expense dialog
                editExpense(expense)
            }
        }

        // Add all views to the TableRow
        tableRow.apply {
            addView(nameTextView)
            addView(dailyTextView)
            addView(monthlyTextView)
            addView(annualTextView)
            addView(hideButton)
            addView(deleteButton)
            addView(editButton)
        }

        // Set the background color if the expense is hidden
        if (expense.isHidden) {
            tableRow.setBackgroundColor(Color.GRAY)
        }

        // Add the TableRow to the TableLayout
        tableLayout.addView(tableRow)
    }

    // Function to update the total expenses displayed
    private fun updateTotal() {
        val totalDaily = expenseList.filter { !it.isHidden }.sumOf { it.daily }
        val totalMonthly = expenseList.filter { !it.isHidden }.sumOf { it.monthly }
        val totalAnnual = expenseList.filter { !it.isHidden }.sumOf { it.annual }

        // Update the TextViews with the new totals
        findViewById<TextView>(R.id.totalDailyTextView).text = "Total Daily: $%.2f".format(totalDaily)
        findViewById<TextView>(R.id.totalMonthlyTextView).text = "Total Monthly: $%.2f".format(totalMonthly)
        findViewById<TextView>(R.id.totalAnnualTextView).text = "Total Annual: $%.2f".format(totalAnnual)
    }

    // Function to edit an existing expense
    private fun editExpense(expense: Expense) {
        // Inflate the custom dialog layout for editing
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_expense, null)

        // Build the AlertDialog
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Edit Expense")

        // Show the dialog
        val alertDialog = builder.create()
        alertDialog.show()

        // Reference the input fields and button in the dialog
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val dailyEditText = dialogView.findViewById<EditText>(R.id.dailyEditText)
        val monthlyEditText = dialogView.findViewById<EditText>(R.id.monthlyEditText)
        val annualEditText = dialogView.findViewById<EditText>(R.id.annualEditText)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

        // Pre-fill the fields with current expense data
        nameEditText.setText(expense.name)
        dailyEditText.setText("%.2f".format(expense.daily))
        monthlyEditText.setText("%.2f".format(expense.monthly))
        annualEditText.setText("%.2f".format(expense.annual))

        // Handle the "Save" button click
        saveButton.setOnClickListener {
            // Update the expense object with new data
            expense.name = nameEditText.text.toString().trim()
            expense.daily = dailyEditText.text.toString().toDoubleOrNull() ?: 0.0
            expense.monthly = monthlyEditText.text.toString().toDoubleOrNull() ?: 0.0
            expense.annual = annualEditText.text.toString().toDoubleOrNull() ?: 0.0

            // Refresh the table to reflect changes
            refreshTable()
            // Update the totals
            updateTotal()

            // Dismiss the dialog
            alertDialog.dismiss()
        }
    }

    // Function to refresh the entire table display
    private fun refreshTable() {
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)
        // Remove all rows except the header row (index 0)
        tableLayout.removeViews(1, tableLayout.childCount - 1)

        // Re-add all expenses to the table
        expenseList.forEach { addRow(it) }
    }
}