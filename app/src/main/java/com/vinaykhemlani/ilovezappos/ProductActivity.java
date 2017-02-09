package com.vinaykhemlani.ilovezappos;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.vinaykhemlani.ilovezappos.API.APIResponse;
import com.vinaykhemlani.ilovezappos.API.SearchResult;
import com.vinaykhemlani.ilovezappos.API.ZapposAPIService;
import com.vinaykhemlani.ilovezappos.databinding.ActivityProductBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ProductActivity extends AppCompatActivity implements Callback<APIResponse> {

    public static final String DATA_IDENTIFIER = "com.khemlani.ilovezappos.PRODUCT_DATA";
    public static final String SEARCH_TERM_IDENTIFER = "com.khemlani.ilovezappos.SEARCH_TERM";

    private static final String PRODUCT_PAGE_URL = "http://www.zappos.com/product/";

    private String productId;
    private String searchTerm;
    private SearchResult product = null;

    private final long AUTOSCROLL_DURATION = 3000;  // 3s on each page
    private ViewPager mViewPager;
    private boolean pagerMoved = false;
    private Handler mViewPagerAutoscrollHandler = null;
    private Runnable mViewPagerAnimator = null;

    private ProgressDialog progressDialog;

    /**
     * Creates the activity and either binds data to the layout or initiates a request
     * to get data.
     *
     * product and searchTerm are stored as global variables so they can be
     * accessed when sharing.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If the user searched the term and we have product data, bind the data to the layout.
        if (getIntent().hasExtra(ProductActivity.DATA_IDENTIFIER)) {

            Intent i = getIntent();
            product = (SearchResult) i.getSerializableExtra(ProductActivity.DATA_IDENTIFIER);
            searchTerm = i.getStringExtra(ProductActivity.SEARCH_TERM_IDENTIFER);

            bindProduct();

        } else {
            // Else, the user opened the app through deep linking.
            // User the productId and searchTerm to make an API request and get product data

            // eg. "zappos://product/id=1234567,term=nike" -> productId = 1234567, searchTerm = "nike"
            String url = getIntent().getData().toString();
            url = url.replace("zappos://product/", "");
            String[] parameters = url.split(",");
            if (parameters.length == 2) {

                productId = parameters[0].replace("id=", "");
                searchTerm = parameters[1].replace("term=", "");
                performSearch();

            } else {
                //Notify the user if there was an error with the link provided
                errorFunction();
            }

        }

    }

    /**
     * Pause the autoscrolling of the handler when leaving the activity
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mViewPagerAutoscrollHandler != null)
            mViewPagerAutoscrollHandler.removeCallbacks(mViewPagerAnimator);
    }

    /**
     * Resume the autoscrolling upon return
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mViewPagerAutoscrollHandler != null)
            mViewPagerAutoscrollHandler.postDelayed(mViewPagerAnimator, AUTOSCROLL_DURATION);
    }


    /**
     * Bind the product to the layout file and set it as the content view. Begins to download
     * addition data (description and picture)
     */
    private void bindProduct() {

        // "R.layout.activity_product" -> ActivityProductBinding
        ActivityProductBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_product);
        binding.setProduct(product);
        initializeFAB();

        getAdditionalProductData(product.getProductId());
    }

    // If the user was given a deep link, use the API to search for the

    /**
     * Makes a request to the Zappos API based on the searchTerm.
     * Used when user was brought to the app via a deep link.
     */
    private void performSearch() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ZapposAPIService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ZapposAPIService apiService = retrofit.create(ZapposAPIService.class);
        Call<APIResponse> response = apiService.performSearch(searchTerm);
        response.enqueue(ProductActivity.this);

        progressDialog = ProgressDialog.show(this, "", "Loading product...");

    }

    /**
     * Sets onClick listener for "Add to Cart" Floating Action Button
     * Also sets initial xScale to 0.0 and changes to 1.0 when animating
     * Creates a stretching out effect on the notification text view.
     */
    private void initializeFAB() {

        final TextView notificationText = (TextView) findViewById(R.id.add_to_cart);
        if (notificationText != null)
            notificationText.setScaleX(0.0f);   // Hides notification text initially

        final Animation a =
                AnimationUtils.loadAnimation(ProductActivity.this, R.anim.added_to_shopping_cart_anim);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null)
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    a.reset();
                    a.setFillAfter(true);
                    a.setFillBefore(true);

                    if (notificationText != null) {
                        // Unhide and then animate
                        notificationText.setScaleX(1.0f);
                        notificationText.clearAnimation();
                        notificationText.startAnimation(a);
                    }
                }
            });

    }

    /**
     * Makes a GET request to retrieve source code from product site on Zappos.com
     * Passes html source onto callback to be handled and processed.
     * @param productId ID corresponding to the product displayed
     */
    private void getAdditionalProductData(String productId) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = PRODUCT_PAGE_URL + productId + "/";

        // Request a string response (of source code) from the provided URL.
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorFunction();
                    }
                }
        );
        queue.add(stringRequest);   // Begins requests
    }

    /**
     * Takes source code of product page from Zappos.com and retrieves description
     * and MULTIVIEW image urls.
     *
     * For description, only styles list items and strong tags, all others ignored.
     * @param response
     */
    private void parseResponse(String response) {

        // Find description
        // ie. <span class="description summary"><ul> ... </ul></span>
        final String DESCRIPTION_PREFIX = "<span class=\"description summary\"><ul>";
        final String DESCRIPTION_SUFFIX = "</ul></span>";

        int startIndex = response.indexOf(DESCRIPTION_PREFIX) + DESCRIPTION_PREFIX.length();
        int endIndex = response.indexOf(DESCRIPTION_SUFFIX, startIndex);
        String descriptionSubstring = response.substring(startIndex, endIndex);
        descriptionSubstring = descriptionSubstring
                .replaceAll("\n","")
                // Add bullets to each list item
                .replaceAll("<li.*?>", "&mdash;  ")
                // Add line breaks after each list time
                .replaceAll("</li>", "!br!")
                // Add indentation to inner lists (through nonbreaking spaces)
                .replaceAll("<ul.*?>", "!br!&nbsp&nbsp&nbsp&nbsp")
                // Change strong tags to corresponding placeholders (all tags will be removed)
                .replaceAll("<strong>", "!strong!")
                .replaceAll("</strong>", "!/strong!")
                // Remove all other tags
                .replaceAll("<.*?>", "")
                // Change line breaks and strong placeholders into html tags
                .replaceAll("!br!", "<br>")
                .replaceAll("!strong!", "<strong><b>")
                .replaceAll("!/strong!", "</></strong>");

        TextView productDescription = (TextView) findViewById(R.id.product_description);
        if (productDescription != null)
            productDescription.setText(Html.fromHtml(descriptionSubstring));



        // Find urls - converts thumbnail url into multiview (larger) image RegEx and find
        // images that match pattern
        // eg. http://a1.zassets.com/images/z/8/7/9/9/8/3/879983-t-THUMBNAIL.jpg ->
        //          http://a1.zassets.com/images/z/8/7/9/9/8/3/879983-p-MULTIVIEW.jpg,
        //          http://a1.zassets.com/images/z/8/7/9/9/8/3/879983-1-MULTIVIEW.jpg,
        //          http://a1.zassets.com/images/z/8/7/9/9/8/3/879983-2-MULTIVIEW.jpg,
        //          http://a1.zassets.com/images/z/8/7/9/9/8/3/879983-3-MULTIVIEW.jpg, etc.
        String urlRegex = product.getThumbnailImageUrl().replaceFirst("t-THUMBNAIL.jpg", ".+-MULTIVIEW.jpg");
        Pattern urlPattern = Pattern.compile(urlRegex);

        List<String> urls = new ArrayList<>();

        Matcher m = urlPattern.matcher(response);
        while (m.find()) {
            String picUrl = m.group(0);
            urls.add(picUrl);
        }

        // Initialize ViewPager based on images found
        initializeViewPager(urls);

    }

    /**
     * Sets adapter and circle indicators for the ViewPager. Also starts autoscrolling.
     *
     * @param urls list of image URLs
     */
    private void initializeViewPager(List<String> urls) {

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ProductImageAdapter(this, urls));
        // If the viewPager was toucher, set flag to stop autoscrolling
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                pagerMoved = true;
                return false;
            }
        });

        // Connect indicators to viewPager
        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        if (indicator != null)
            indicator.setViewPager(mViewPager);

        // Hide placeholder image that was loaded initially
        // TODO consider showing progressbar instead
        View placeholderImage = findViewById(R.id.placeholderImage);
        if (placeholderImage != null)
            placeholderImage.setVisibility(View.GONE);

        mViewPager.setVisibility(View.VISIBLE);

        // Start autoscrolling
        mViewPagerAutoscrollHandler = new Handler();
        mViewPagerAnimator = new Runnable() {
            public void run() {
                if (!pagerMoved) {

                    int currentItem = mViewPager.getCurrentItem();
                    if (currentItem < mViewPager.getAdapter().getCount() - 1) {
                        mViewPager.setCurrentItem(currentItem + 1);
                    } else {
                        mViewPager.setCurrentItem(0);
                    }

                    if (mViewPagerAnimator != null) {
                        mViewPagerAutoscrollHandler.postDelayed(mViewPagerAnimator, AUTOSCROLL_DURATION);
                    }
                }
            }
        };
        mViewPagerAutoscrollHandler.postDelayed(mViewPagerAnimator, AUTOSCROLL_DURATION);

    }

    /**
     * Fires intent to take user to zappos.com
     * @param v "View More on Zappos.com" button
     */
    public void viewProductSite(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(product.getProductUrl()));
        startActivity(i);
    }

    /**
     * Fires intent to allow user to share product view sharing application of their choice
     * @param v "Share This with a Friend" button
     */
    public void shareProduct(View v) {

        final String PRODUCT_SHARE_SCHEMA = "zappos://product/";

        String deepLink = PRODUCT_SHARE_SCHEMA + "id=" + product.getProductId() + ",term=" + searchTerm;

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/html");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml("<p>Check out this cool product! " + deepLink + "</p>"));
        startActivity(Intent.createChooser(sharingIntent,"Share using"));

    }

    /* ---------- Callback Implementation Functions ---------- */

    /**
     * Callback fired by retrofit2 if response is successfully received.
     * If response is successful, move to next activity.
     * Else, notify user with errorFunction.
     *
     * @param call      Original call that was made to API
     * @param response  Response from API request
     */
    @Override
    public void onResponse(Call<APIResponse> call, retrofit2.Response<APIResponse> response) {

        // Dismiss progress dialog
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if (response.isSuccessful()) {

            List<SearchResult> results = response.body().getResults();
            for (SearchResult r : results) {
                if (r.getProductId().equals(productId)) {
                    product = r;
                    break;
                }
            }

            if (product == null) {
                Toast.makeText(this, R.string.product_activity_product_error, Toast.LENGTH_LONG).show();
            } else {
                bindProduct();
            }

        } else {
            errorFunction();
        }

    }

    /**
     * Callback fired if API requests fails. Notifies user and recommends
     * trying again.
     *
     * @param call  Original call made to API
     * @param t     Error thrown by request
     */
    @Override
    public void onFailure(Call<APIResponse> call, Throwable t) {

        // Dismiss progress dialog
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        System.err.println("Failed.");
        t.printStackTrace();
        errorFunction();

    }

    /* ---------- Activity Utility Functions ---------- */

    /**
     * Displays a Toast notification when there is a problem with the API response.
     */
    private void errorFunction() {
        Toast.makeText(this, R.string.search_activity_search_error, Toast.LENGTH_LONG).show();
    }

}

/**
 * Adapter used for scrolling images ViewPager
 */
class ProductImageAdapter extends PagerAdapter {

    private Context mContext;
    private List<String> imageUrls;

    ProductImageAdapter(Context context, List<String> urls) {
        mContext = context;
        imageUrls = urls;
    }

    /**
     * Uses Picasso to load images and manage image storage.
     * @param container
     * @param position
     * @return
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        LinearLayout layout =
                (LinearLayout) inflater.inflate(R.layout.product_pager_imageview, container, false);
        ImageView imageView =
                (ImageView) layout.findViewById(R.id.image);

        Picasso.with(mContext)
                .load(imageUrls.get(position))
                .placeholder(R.drawable.zappos_loading_placeholder)
                .into(imageView);

        container.addView(layout);

        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        container.removeView((View) view);
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /**
     * Only showing images
     *
     * @param position
     * @return
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }
}
