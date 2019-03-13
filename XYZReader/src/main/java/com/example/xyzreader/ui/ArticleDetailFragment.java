package com.example.xyzreader.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.content.Context;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ShareCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.PrecomputedText;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.design.widget.AppBarLayout;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.squareup.picasso.Picasso;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = "ArticleDetailFragment";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private Typeface typeface;
    private String format = "yyyy-MM-dd'T'HH:mm:s";
    private Locale locale = getLocal();
    private SimpleDateFormat dateFormat = new SimpleDateFormat(format, locale);
    private SimpleDateFormat outputFormat = new SimpleDateFormat(format, locale);
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     * //     * @param aLong
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    static void asyncSetText(final TextView tv, final String s, Executor ex) {
        final PrecomputedText.Params params = tv.getTextMetricsParams();
        final Reference reference = new WeakReference<>(tv);
        ex.execute(new Runnable() {
            @Override
            public void run() {
                TextView textView = (TextView) reference.get();
                if (textView == null)
                    return;
                final PrecomputedText precomputedText = PrecomputedText.create(
                        s, params);
                tv.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView2 = (TextView) reference.get();
                        if (textView2 == null)
                            return;
                        tv.setText(precomputedText);
                    }
                });
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Objects.requireNonNull(getArguments()).containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {

        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container,
                false);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(
                        Objects.requireNonNull(getActivity()))
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        return mRootView;
    }

    Locale getLocal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            return Resources.getSystem().getConfiguration().locale;
        }
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        final TextView titleView = mRootView.findViewById(R.id.article_title);
        final TextView bylineView = mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = mRootView.findViewById(R.id.article_body);
        ImageView imageView = mRootView.findViewById(R.id.photo);
        titleView.setTypeface(typeface);
        bylineView.setTypeface(typeface);
        bodyView.setTypeface(typeface);

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                bylineView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " "
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)));
            } else {
                bylineView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate)
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                String string = Html.fromHtml(mCursor.getString(
                        ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)",
                        "<br />")).toString();
                asyncSetText(bodyView, string, Objects.requireNonNull(
                        getActivity()).getMainExecutor());
            } else {
                bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)
                        .replaceAll("(\r\n|\n)", "<br />")));
            }
            final Toolbar toolbar =
                    mRootView.findViewById(R.id.toolbar);
            getActivityCast().setSupportActionBar(toolbar);
            final String title = titleView.getText().toString();
            toolbar.setTitle("");
            AppBarLayout appBarLayout = mRootView.findViewById(
                    R.id.app_bar_layout);
            appBarLayout.addOnOffsetChangedListener(
                    new AppBarLayout.OnOffsetChangedListener() {
                        @Override
                        public void onOffsetChanged(final AppBarLayout appBarLayout,
                                                    final int verticalOffset) {
                            toolbar.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (Math.abs(verticalOffset) -
                                            appBarLayout.getTotalScrollRange() == 0) {
                                        toolbar.setTitle(title);
                                    }
                                    toolbar.setTitle("");
                                }
                            });
                        }
                    });
            String imageurl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            Log.i("articledetailfragment", imageurl);
            Picasso.get().load(imageurl).into(imageView);
        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A");
            bodyView.setText("N/A");
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }
        bindViews();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() != null)
            typeface = Typeface.createFromAsset(getActivity().getAssets(),
                    "Rosario-Regular.ttf");
    }
}