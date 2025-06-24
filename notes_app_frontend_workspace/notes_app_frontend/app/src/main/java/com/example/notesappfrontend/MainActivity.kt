package com.example.notesappfrontend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import android.widget.Button
import android.widget.TextView
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

// PUBLIC_INTERFACE
data class Note(
    var id: Long,
    var title: String,
    var body: String
)

/**
 * Adapter for displaying notes in a minimalistic card style.
 */
class NotesAdapter(
    private var notes: MutableList<Note>,
    private val onEdit: (Note) -> Unit,
    private val onDelete: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val noteTitle: TextView = view.findViewById(R.id.noteTitle)
        val noteBody: TextView = view.findViewById(R.id.noteBody)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): NoteViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note_card, parent, false)
        return NoteViewHolder(v)
    }

    override fun getItemCount() = notes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.noteTitle.text = note.title
        holder.noteBody.text = note.body

        holder.view.setOnClickListener {
            onEdit(note)
        }

        holder.view.setOnLongClickListener {
            onDelete(note)
            true
        }
    }

    // PUBLIC_INTERFACE
    fun updateNotes(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }

    // PUBLIC_INTERFACE
    fun deleteNote(note: Note) {
        notes.remove(note)
        notifyDataSetChanged()
    }

    // PUBLIC_INTERFACE
    fun addNote(note: Note) {
        notes.add(0, note)
        notifyItemInserted(0)
    }

    // PUBLIC_INTERFACE
    fun updateNote(updated: Note) {
        val idx = notes.indexOfFirst { it.id == updated.id }
        if (idx != -1) {
            notes[idx] = updated
            notifyItemChanged(idx)
        }
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var addNoteFAB: FloatingActionButton
    private lateinit var adapter: NotesAdapter
    private var notesList = mutableListOf<Note>()
    private var noteIdCounter = 0L // Simple in-memory id management

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)

        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        addNoteFAB = findViewById(R.id.addNoteFAB)
        adapter = NotesAdapter(notesList,
            onEdit = { note -> showEditNoteDialog(note) },
            onDelete = { note -> showDeleteConfirmation(note) }
        )
        notesRecyclerView.layoutManager = LinearLayoutManager(this)
        notesRecyclerView.adapter = adapter

        addNoteFAB.setOnClickListener {
            showEditNoteDialog(null)
        }

        // Show empty text if there are no notes
        updateEmptyNotesView()
    }

    // PUBLIC_INTERFACE
    private fun showEditNoteDialog(note: Note?) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_edit_note, null, false)

        val titleEditText = dialogView.findViewById<EditText>(R.id.titleEditText)
        val bodyEditText = dialogView.findViewById<EditText>(R.id.bodyEditText)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        if (note != null) {
            titleEditText.setText(note.title)
            bodyEditText.setText(note.body)
        }
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val body = bodyEditText.text.toString().trim()
            if (title.isEmpty()) {
                titleEditText.error = getString(R.string.note_title_hint)
                return@setOnClickListener
            }
            if (note == null) {
                // Create new note
                val newNote = Note(
                    id = ++noteIdCounter,
                    title = title,
                    body = body
                )
                notesList.add(0, newNote)
                adapter.addNote(newNote)
                Toast.makeText(this, getString(R.string.note_saved), Toast.LENGTH_SHORT).show()
            } else {
                // Update
                note.title = title
                note.body = body
                adapter.updateNote(note)
                Toast.makeText(this, getString(R.string.note_updated), Toast.LENGTH_SHORT).show()
            }
            updateEmptyNotesView()
            alertDialog.dismiss()
        }

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    // PUBLIC_INTERFACE
    private fun showDeleteConfirmation(note: Note) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.confirm_delete))
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                notesList.remove(note)
                adapter.deleteNote(note)
                updateEmptyNotesView()
                Toast.makeText(this, getString(R.string.note_deleted), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // PUBLIC_INTERFACE
    private fun updateEmptyNotesView() {
        // Add an empty view if notes list is empty (via RecyclerView's AdapterDataObserver)
        if (notesList.isEmpty()) {
            if (notesRecyclerView.findViewById<TextView>(R.id.emptyNotesText) == null) {
                val emptyView = TextView(this).apply {
                    id = R.id.emptyNotesText
                    layoutParams = RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.MATCH_PARENT
                    )
                    text = getString(R.string.empty_notes)
                    textSize = 16f
                    setTextColor(resources.getColor(R.color.greyText, null))
                    gravity = android.view.Gravity.CENTER
                }
                (notesRecyclerView.parent as? android.view.ViewGroup)?.addView(emptyView)
                notesRecyclerView.visibility = View.GONE
            }
        } else {
            (notesRecyclerView.parent as? android.view.ViewGroup)?.findViewById<TextView>(R.id.emptyNotesText)
                ?.let { (notesRecyclerView.parent as android.view.ViewGroup).removeView(it) }
            notesRecyclerView.visibility = View.VISIBLE
        }
    }
}
