package tr.xip.wanikani;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import tr.xip.wanikani.managers.PrefManager;
import tr.xip.wanikani.managers.ThemeManager;
import tr.xip.wanikani.utils.Fonts;

/**
 * Created by xihsa_000 on 3/15/14.
 */
public class Browser extends ActionBarActivity {

    public static final String ARG_ACTION = "action";
    public static final String ARG_ITEM = "item";
    public static final String ARG_ITEM_TYPE = "itemtype";
    public static final String ACTION_ITEM_DETAILS = "itemdetails";
    public static final String ACTION_LESSON = "lesson";
    public static final String ACTION_REVIEW = "reviews";
    static final String LESSON_URL = "https://www.wanikani.com/lesson/session";
    static final String REVIEW_URL = "https://www.wanikani.com/review/session";
    static final String RADICAL_URL = "https://www.wanikani.com/radicals/";
    static final String KANJI_URL = "https://www.wanikani.com/kanji/";
    static final String VOCABULARY_URL = "https://www.wanikani.com/vocabulary/";
    WebView mWebview;
    ThemeManager themeMan;
    PrefManager prefMan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mWebview = new WebView(this);
        themeMan = new ThemeManager(this);
        prefMan = new PrefManager(this);

        super.onCreate(savedInstanceState);
        setContentView(mWebview);

        getSupportActionBar().setIcon(R.drawable.ic_wanikani_stamp);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mActionBarView = inflater.inflate(R.layout.actionbar_custom, null);

        TextView mActionBarTitleText = (TextView) mActionBarView.findViewById(R.id.actionbar_custom_title_text);

        mActionBarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setCustomView(mActionBarView);

        mWebview.getSettings().setJavaScriptEnabled(true);

        mWebview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e("WANIKANI BROWSER", "Error code: " + errorCode + "; Description: " + description + "; Url: " + failingUrl);
            }
        });

        Intent intent = getIntent();
        String action = intent.getStringExtra(ARG_ACTION);
        String itemType = intent.getStringExtra(ARG_ITEM_TYPE);
        String item = intent.getStringExtra(ARG_ITEM);

        if (action.equals(ACTION_LESSON)) {
            mWebview.loadUrl(LESSON_URL);
            mActionBarTitleText.setText(R.string.ab_title_lessons);

            setOrientation(prefMan.getLessonsScreenOrientation());
        }
        if (action.equals(ACTION_REVIEW)) {
            mWebview.loadUrl(REVIEW_URL);
            mActionBarTitleText.setText(R.string.ab_title_reviews);

            setOrientation(prefMan.getReviewsScreenOrientation());
        }

        if (action.equals(ACTION_ITEM_DETAILS)) {
            if (itemType.equals(ItemDetailsActivity.TYPE_RADICAL)) {
                mWebview.loadUrl(RADICAL_URL + item);
                mActionBarTitleText.setText(item);
            }
            if (itemType.equals(ItemDetailsActivity.TYPE_KANJI)) {
                mWebview.loadUrl(KANJI_URL + item);
                mActionBarTitleText.setText(item);
                mActionBarTitleText.setTypeface(new Fonts().getKanjiFont(this));
            }
            if (itemType.equals(ItemDetailsActivity.TYPE_VOCABULARY)) {
                mWebview.loadUrl(VOCABULARY_URL + item);
                mActionBarTitleText.setText(item);
                mActionBarTitleText.setTypeface(new Fonts().getKanjiFont(this));
            }
        }


    }

    private void setOrientation(String orientation) {
        if (orientation.equals("Portrait"))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else if (orientation.equals("Landscape"))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    @Override
    public boolean onNavigateUp() {
        super.onBackPressed();
        return true;
    }
}
