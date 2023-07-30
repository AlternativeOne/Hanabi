package com.lexoff.animediary.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.preference.PreferenceManager;

import com.lexoff.animediary.Api;
import com.lexoff.animediary.Constants;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.CustomSpinnerArrayAdapter;
import com.lexoff.animediary.Extractor.SearchCategory;
import com.lexoff.animediary.Info.AnimeSearchItemInfo;
import com.lexoff.animediary.Info.CompanySearchItemInfo;
import com.lexoff.animediary.Info.SearchInfo;
import com.lexoff.animediary.Info.SearchItemInfo;
import com.lexoff.animediary.Enum.InfoSourceType;
import com.lexoff.animediary.Util.NavigationUtils;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Adapter.SearchResultsAdapter;
import com.lexoff.animediary.Util.Utils;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SearchFragment extends BaseFragment {

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Disposable currentWorker;

    private Spinner categorySpinner;
    private EditText searchEditText;

    private RecyclerView resultsView;

    private SearchRequest searchRequest=new SearchRequest();

    private int currentPage, maxPage;

    private ArrayList<SearchRequest> searchStack=new ArrayList<>();

    private Handler mainHandler;

    private SharedPreferences defPrefs;

    public SearchFragment() {
        //empty
    }

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainHandler=new Handler(Looper.myLooper(), null);

        defPrefs=PreferenceManager.getDefaultSharedPreferences(requireContext());

        currentPage=1;
        maxPage=1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        //set margins of statusbar and navbar
        //post because if not then padding will not be set to rootview of fragments opened from AnimeFragment
        rootView.post(()->{
            int statusbarHeight = Utils.getStatusBarHeight(requireContext());
            int navbarHeight = Utils.getNavBarHeight(requireContext());
            rootView.setPadding(0, statusbarHeight, 0, navbarHeight);
        });

        resultsView = rootView.findViewById(R.id.results_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        resultsView.setLayoutManager(layoutManager);

        resultsView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (resultsView.getAdapter() == null)
                    return;

                if (resultsView.getAdapter().getItemCount() == 0)
                    return;

                if (currentPage >= maxPage)
                    return;

                if (dy > 0) {
                    int pastVisibleItems, visibleItemCount, totalItemCount;
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

                    pastVisibleItems = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        doMoreSearch(searchRequest.searchQuery, searchRequest.searchCategory);
                    }
                }
            }
        });

        categorySpinner = rootView.findViewById(R.id.search_cat_spinner);
        CustomSpinnerArrayAdapter adapter = new CustomSpinnerArrayAdapter(requireContext(), R.layout.custom_spinner_item, new String[]{getString(R.string.search_category_anime_title), getString(R.string.search_category_company_title)});
        categorySpinner.setAdapter(adapter);

        searchEditText = rootView.findViewById(R.id.search_edit_text);
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            String searchStr = searchEditText.getText().toString();
            SearchCategory searchCat = Utils.intToSearchCategory(categorySpinner.getSelectedItemPosition());

            Utils.hideKeyboard(searchEditText);

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (!searchStr.isEmpty() && (!searchStr.equals(searchRequest.searchQuery) || searchCat!=searchRequest.searchCategory)) {
                    searchRequest = new SearchRequest();
                    searchRequest.searchQuery = searchStr;
                    searchRequest.searchCategory = searchCat;

                    if (searchStr.contains("|")) {
                        searchStr = searchStr.substring(searchStr.lastIndexOf("|") + 1);
                    }

                    doSearch(searchStr, searchCat);

                    maybeAddToStack(searchRequest);
                }
            }

            return false;
        });

        ImageView clearSearchButton = rootView.findViewById(R.id.clear_search_btn);
        clearSearchButton.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                searchEditText.setText("");

                Utils.showKeyboard(searchEditText);
            });
        });

        Utils.showKeyboard(searchEditText);
    }

    @Override
    public void onResume() {
        super.onResume();

        //to redraw items
        if (resultsView.getAdapter() != null)
            resultsView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onPause(){
        super.onPause();

        Utils.hideKeyboard(searchEditText);
    }

    @Override
    public void onHiddenChanged (boolean hidden) {
        super.onHiddenChanged(hidden);

        if (hidden) {
            mainHandler.postDelayed(() -> Utils.hideKeyboard(searchEditText), 250);
        } else {
            if (resultsView.getAdapter() == null)
                Utils.showKeyboard(searchEditText);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (searchStack.size()<2) return false;

        SearchRequest prevSearchReq=removeLastAndGet(searchStack);
        doSearch(prevSearchReq.searchQuery, prevSearchReq.searchCategory);

        String curSearchStr=searchEditText.getText().toString();
        if (curSearchStr.contains("|")){
            searchEditText.setText(curSearchStr.substring(0, curSearchStr.lastIndexOf("|")));
        } else {
            searchEditText.setText(prevSearchReq.searchQuery);
        }

        SearchCategory prevSearchCat=prevSearchReq.searchCategory;
        categorySpinner.setSelection(Utils.searchCategoryToInt(prevSearchCat));

        return true;
    }

    private void doSearch(String searchStr, SearchCategory searchCat){
        if (isLoading.get()) return;

        isLoading.set(true);

        updateLoading();

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> (new Api()).getSearchInfo(searchStr, searchCat, currentPage))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final SearchInfo result) -> {
                    isLoading.set(false);

                    updateLoading();

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);

                    handleError(throwable);
                });
    }

    private void handleResult(SearchInfo info){
        maxPage=info.getMaxPage();

        SearchResultsAdapter adapter=new SearchResultsAdapter(requireContext(), info.getItems(), new CustomOnItemClickListener(){
            @Override
            public void onClick(View v, int position){
                SearchItemInfo item=((SearchResultsAdapter) resultsView.getAdapter()).getItem(position);

                if (item instanceof AnimeSearchItemInfo) {
                    NavigationUtils.openAnimeFragment(requireActivity(), ((AnimeSearchItemInfo) item).getMalid(), InfoSourceType.REMOTE);
                } else if (item instanceof CompanySearchItemInfo){
                    NavigationUtils.openCompanyFragment(requireActivity(), ((CompanySearchItemInfo) item).getCmalid());
                }
            }
        });
        adapter.setAdditionalParams(defPrefs.getBoolean(Constants.SHOW_ADDED_TO_BADGE, true));
        resultsView.setAdapter(adapter);
    }

    private void doMoreSearch(String searchStr, SearchCategory searchCat){
        if (searchStr==null || searchStr.isEmpty()) return;

        if (isLoading.get()) return;

        isLoading.set(true);

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> (new Api()).getSearchInfo(searchStr, searchCat, currentPage+=1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final SearchInfo result) -> {
                    isLoading.set(false);

                    handleMoreItems(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);

                    handleError(throwable);
                });
    }

    private void handleMoreItems(SearchInfo info){
        ((SearchResultsAdapter) resultsView.getAdapter()).addItems(info.getItems());
    }

    private void updateLoading(){
        showOrHideMainLayout(!isLoading.get());
        hideErrorLayout();
        showOrHideLoadingLayout(isLoading.get());
    }

    private void showOrHideMainLayout(boolean show){
        View mainLayout=getView().findViewById(R.id.main_layout);
        mainLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showOrHideLoadingLayout(boolean show){
        View loadingLayout=getView().findViewById(R.id.loading_layout);

        if (show) {
            ImageView loadingSplashView = loadingLayout.findViewById(R.id.loading_splash);
            loadingSplashView.setImageURI(null);
            loadingSplashView.setImageURI(Utils.resolveBumperUri(requireContext()));
        }

        ProgressBar loadingProgressBar=loadingLayout.findViewById(R.id.loading_progressbar);
        loadingProgressBar.setPadding(loadingProgressBar.getPaddingLeft(), loadingProgressBar.getPaddingTop(), loadingProgressBar.getPaddingRight(), Utils.getNavBarHeight(requireContext()));

        loadingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showErrorLayout(String title, String message){
        View errorLayout=getView().findViewById(R.id.error_layout);
        errorLayout.setVisibility(View.VISIBLE);

        TextView titleView=errorLayout.findViewById(R.id.error_title);
        titleView.setText(title);

        TextView messageView=errorLayout.findViewById(R.id.error_message);
        messageView.setText(message);

        TextView reloadButton=errorLayout.findViewById(R.id.error_reload_btn);
        reloadButton.setOnClickListener(v -> doSearch(searchRequest.searchQuery, searchRequest.searchCategory));
    }

    private void hideErrorLayout(){
        View errorLayout=getView().findViewById(R.id.error_layout);
        errorLayout.setVisibility(View.GONE);
    }

    private void handleError(Throwable e){
        showOrHideLoadingLayout(false);
        showErrorLayout(getString(R.string.error_happened), e.getMessage());
    }

    private void maybeAddToStack(SearchRequest r){
        if (searchStack.isEmpty() || !compareToLast(r, searchStack)) {
            searchStack.add(r);
        }
    }

    private SearchRequest removeLastAndGet(ArrayList<SearchRequest> arrayList){
        arrayList.remove(arrayList.size()-1);

        return arrayList.get(arrayList.size()-1);
    }

    private boolean compareToLast(SearchRequest r, ArrayList<SearchRequest> arrayList){
        SearchRequest r2=arrayList.get(arrayList.size()-1);

        return r.searchQuery.toLowerCase().equals(r2.searchQuery.toLowerCase()) && r.searchCategory==r2.searchCategory;
    }

    private class SearchRequest{
        String searchQuery="";
        SearchCategory searchCategory=SearchCategory.ANIME;
    }

}
