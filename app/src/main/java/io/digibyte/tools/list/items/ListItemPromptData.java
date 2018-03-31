package io.digibyte.tools.list.items;

import io.digibyte.R;
import io.digibyte.tools.list.ListItemData;
import io.digibyte.tools.manager.PromptManager;

public class ListItemPromptData extends ListItemData
{
    public final PromptManager.PromptItem promptItem;

    public ListItemPromptData(PromptManager.PromptItem aPromptItem)
    {
        super(R.layout.list_item_prompt);
        this.promptItem = aPromptItem;
    }
}
