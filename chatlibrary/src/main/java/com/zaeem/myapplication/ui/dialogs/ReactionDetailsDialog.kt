package com.zaeem.myapplication.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zaeem.myapplication.common.enums.Reaction
import com.zaeem.myapplication.common.extensions.applicationContext
import com.zaeem.myapplication.common.extensions.lazyActivityViewModel
import com.zaeem.myapplication.common.injector
import com.zaeem.myapplication.data.models.MessageListViewItem
import com.zaeem.myapplication.databinding.DialogReactionDetailsBinding
import com.zaeem.myapplication.databinding.RowReactionDetailsItemBinding

class ReactionDetailsDialog : BaseBottomSheetDialogFragment() {

    lateinit var binding: DialogReactionDetailsBinding

    val messageListViewModel by lazyActivityViewModel {
        val conversationSid = requireArguments().getString(ARGUMENT_CONVERSATION_SID)!!
        injector.createMessageListViewModel(applicationContext, conversationSid)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogReactionDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val message = messageListViewModel.selectedMessage ?: run {
            dismiss()
            return
        }

        val reactionsView = binding.editReactions.root
        reactionsView.reactions = message.reactions
        messageListViewModel.selfUser.observe(this) { reactionsView.identity = it.identity }

        reactionsView.onChangeListener = {
            messageListViewModel.setReactions(reactionsView.reactions)
            dismiss()
        }

        binding.participantsList.adapter = ReactionDetailsAdapter(message)
    }

    companion object {

        private const val ARGUMENT_CONVERSATION_SID = "ARGUMENT_CONVERSATION_SID"

        fun getInstance(conversationSid: String) = ReactionDetailsDialog().apply {
            arguments = Bundle().apply {
                putString(ARGUMENT_CONVERSATION_SID, conversationSid)
            }
        }
    }
}

private class ReactionDetailsAdapter(message: MessageListViewItem) :
    RecyclerView.Adapter<ReactionDetailsAdapter.ViewHolder>() {

    private val reactions: List<ReactionViewItem>

    init {
        reactions = message.reactions
            .flatMap { (reaction, identityList) ->
                identityList.map { ReactionViewItem(it, reaction) }
            }
            .sortedWith(Comparator { item1, item2 ->
                item1.reaction.sortOrder.compareTo(item2.reaction.sortOrder)
                    .takeIf { it != 0 }
                    ?.let { return@Comparator it }

                return@Comparator item1.username.compareTo(item2.username)
            })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowReactionDetailsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context

        holder.binding.reactionUsername.text = reactions[position].username
        holder.binding.reactionEmoji.text = context.getString(reactions[position].reaction.emoji)
    }

    override fun getItemCount() = reactions.size

    class ViewHolder(val binding: RowReactionDetailsItemBinding) : RecyclerView.ViewHolder(binding.root)

    data class ReactionViewItem(val username: String, val reaction: Reaction)
}
