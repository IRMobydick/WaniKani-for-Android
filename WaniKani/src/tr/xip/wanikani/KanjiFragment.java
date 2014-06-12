package tr.xip.wanikani;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

import java.util.List;

import tr.xip.wanikani.adapters.KanjiAdapter;
import tr.xip.wanikani.api.WaniKaniApi;
import tr.xip.wanikani.api.response.KanjiList;
import tr.xip.wanikani.managers.PrefManager;
import tr.xip.wanikani.managers.ThemeManager;

public class KanjiFragment extends Fragment implements LevelPickerDialogFragment.LevelDialogListener {

    Context context;

    WaniKaniApi apiMan;
    ThemeManager themeMan;
    PrefManager prefMan;

    TextView mMessageTitle;
    TextView mMessageSummary;
    ImageView mMessageIcon;
    ViewFlipper mMessageFlipper;

    StickyGridHeadersGridView mGrid;
    ViewFlipper mListFlipper;

    LinearLayout mLegend;
    LinearLayout mLegendOk;

    LevelPickerDialogFragment mLevelPickerDialog;

    KanjiAdapter mKanjiAdapter;
    List<KanjiList.KanjiItem> kanjiList = null;

    View rootView;

    String LEVEL = "";

    private void hideLegend() {
        mLegend.setVisibility(View.GONE);
    }

    private void showLegend() {
        mLegend.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = getActivity();
        apiMan = new WaniKaniApi(getActivity());
        prefMan = new PrefManager(getActivity());
        themeMan = new ThemeManager(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_radicals, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        rootView = layoutInflater.inflate(R.layout.fragment_kanji, viewGroup, false);

        mLegend = (LinearLayout) rootView.findViewById(R.id.kanji_legend);
        mLegend.setBackgroundColor(getResources().getColor(themeMan.getWindowBackgroundColor()));
        mLegendOk = (LinearLayout) rootView.findViewById(R.id.kanji_legend_ok);

        mGrid = (StickyGridHeadersGridView) rootView.findViewById(R.id.kanji_grid);
        mGrid.setOnItemClickListener(new gridItemClickListener());

        mListFlipper = (ViewFlipper) rootView.findViewById(R.id.kanji_list_flipper);
        mMessageFlipper = (ViewFlipper) rootView.findViewById(R.id.kanji_message_flipper);

        mMessageIcon = (ImageView) rootView.findViewById(R.id.kanji_message_icon);
        mMessageTitle = (TextView) rootView.findViewById(R.id.kanji_message_title);
        mMessageSummary = (TextView) rootView.findViewById(R.id.kanji_message_summary);

        if (!prefMan.isKanjiLegendLearnt()) {
            showLegend();
        }

        mLegendOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLegend();
                prefMan.setKanjiLegendLearnt(true);
            }
        });

        if (Build.VERSION.SDK_INT >= 11)
            new UserLevelTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new UserLevelTask().execute();

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onLevelDialogPositiveClick(DialogFragment dialog, String level) {
        LEVEL = level;
        if (Build.VERSION.SDK_INT >= 11)
            new FetchTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new FetchTask().execute();
    }

    @Override
    public void onLevelDialogResetClick(DialogFragment dialogFragment, String level) {
        if (Build.VERSION.SDK_INT >= 11)
            new UserLevelTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new UserLevelTask().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_level:
                showLevelDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showLevelDialog() {
        if (mLevelPickerDialog != null) {
            mLevelPickerDialog.init(this.getId(), LEVEL);
            mLevelPickerDialog.show(getActivity().getSupportFragmentManager(), "LevelPickerDialogFragment");
        }
    }

    private class FetchTask extends AsyncTask<Void, Void, List<KanjiList.KanjiItem>> {

        protected List<KanjiList.KanjiItem> doInBackground(Void... voids) {
            try {
                kanjiList = apiMan.getKanjiList(LEVEL);
                return kanjiList;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return kanjiList;
        }

        protected void onPostExecute(List<KanjiList.KanjiItem> list) {
            super.onPostExecute(list);

            if (list != null) {
                mKanjiAdapter = new KanjiAdapter(context, list, R.layout.header_level, R.layout.item_kanji);
                mGrid.setAdapter(mKanjiAdapter);

                if (!prefMan.isKanjiLegendLearnt()) {
                    showLegend();
                }

                if (mMessageFlipper.getDisplayedChild() == 1)
                    mMessageFlipper.showPrevious();
            } else {
                mMessageIcon.setImageResource(R.drawable.ic_action_warning);
                mMessageTitle.setText(R.string.no_items_title);
                mMessageSummary.setText(R.string.no_items_summary);

                mGrid.setAdapter(null);

                if (mMessageFlipper.getDisplayedChild() == 0) {
                    mMessageFlipper.showNext();
                }
            }

            if (mListFlipper.getDisplayedChild() == 0)
                mListFlipper.showNext();
        }

        protected void onPreExecute() {
            super.onPreExecute();

        }
    }

    private class UserLevelTask extends AsyncTask<Void, Void, Boolean> {

        protected Boolean doInBackground(Void[] voids) {
            try {
                LEVEL = apiMan.getUser().getLevel() + "";
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                if (Build.VERSION.SDK_INT >= 11)
                    new FetchTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    new FetchTask().execute();

                mLevelPickerDialog = new LevelPickerDialogFragment();
            } else {
                mMessageIcon.setImageResource(R.drawable.ic_action_warning);
                mMessageTitle.setText(R.string.no_items_title);
                mMessageSummary.setText(R.string.no_items_summary);

                if (mMessageFlipper.getDisplayedChild() == 0)
                    mMessageFlipper.showNext();
            }

            if (mListFlipper.getDisplayedChild() == 0)
                mListFlipper.showNext();
        }
    }

    private class gridItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            KanjiList.KanjiItem kanjiItem = mKanjiAdapter.getItem(position);

            Intent intent = new Intent(getActivity(), ItemDetailsActivity.class);
            intent.putExtra(ItemDetailsActivity.ARG_TYPE, ItemDetailsActivity.TYPE_KANJI);
            intent.putExtra(ItemDetailsActivity.ARG_CHARACTER, kanjiItem.getCharacter());
            intent.putExtra(ItemDetailsActivity.ARG_LEVEL, kanjiItem.getLevel());
            getActivity().startActivity(intent);
        }
    }

}