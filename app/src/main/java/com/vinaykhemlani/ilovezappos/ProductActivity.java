package com.vinaykhemlani.ilovezappos;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.vinaykhemlani.ilovezappos.API.SearchResult;
import com.vinaykhemlani.ilovezappos.databinding.ActivityProductBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.vinaykhemlani.ilovezappos.R.id.fab;


public class ProductActivity extends AppCompatActivity {

    public static final String DATA_IDENTIFIER = "com.khemlani.ilovezappos.PRODUCT_DATA";

    private final String PRODUCT_PAGE_URL = "http://www.zappos.com/product/";
    private final String DESCRIPTION_PREFIX = "<span class=\"description summary\"><ul>";
    private final String DESCRIPTION_SUFFIX = "</ul></span>";

    private String productID;
    private String thumbnailURL;

    private final long AUTOSCROLL_DURATION = 3000;  // 3s on each page
    private ViewPager mViewPager;
    private boolean pagerMoved = false;
    private Handler mViewPagerAutoscrollHandler = null;
    private Runnable mViewPagerAnimator = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bind the product passed through the intent to the layout file
        // "R.layout.activity_product" -> ActivityProductBinding

        if (getIntent().hasExtra(ProductActivity.DATA_IDENTIFIER)) {

            ActivityProductBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_product);
            SearchResult result = (SearchResult) getIntent().getSerializableExtra(ProductActivity.DATA_IDENTIFIER);
            binding.setProduct(result);

            productID = result.getProductId();
            thumbnailURL = result.getThumbnailImageUrl();

            getAdditionalProductData(result.getProductId());

        }
        //TODO handle case where app opens through schema and hasn't loaded data


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mViewPagerAutoscrollHandler != null)
            mViewPagerAutoscrollHandler.removeCallbacks(mViewPagerAnimator);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mViewPagerAutoscrollHandler != null)
            mViewPagerAutoscrollHandler.postDelayed(mViewPagerAnimator, AUTOSCROLL_DURATION);
    }

    private void getAdditionalProductData(String productId) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = PRODUCT_PAGE_URL + productId + "/";

        // Request a string response from the provided URL.
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

                    }
                }
        );
        queue.add(stringRequest);
    }

    private void parseResponse(String response) {

        int startIndex = response.indexOf(DESCRIPTION_PREFIX) + DESCRIPTION_PREFIX.length();
        int endIndex = response.indexOf(DESCRIPTION_SUFFIX, startIndex);
        String descriptionSubstring = response.substring(startIndex, endIndex);
        descriptionSubstring = descriptionSubstring
                .replaceAll("\n","")
                .replaceAll("<li.*?>", "&mdash;  ")
                .replaceAll("</li>", "!br!")
                .replaceAll("<ul.*?>", "!br!&nbsp&nbsp&nbsp&nbsp")
                .replaceAll("</ul>", "")
                .replaceAll("<strong>", "!strong!")
                .replaceAll("</strong>", "!/strong!")
                .replaceAll("<.*?>", "")
                .replaceAll("!br!", "<br>")
                .replaceAll("!strong!", "<strong><b>")
                .replaceAll("!/strong!", "</></strong>");

        TextView productDescription = (TextView) findViewById(R.id.product_description);
        productDescription.setText(Html.fromHtml(descriptionSubstring));

        //Converts thumbail url into multiview (larger) image RegEx
        String urlRegex = thumbnailURL.replaceFirst("t-THUMBNAIL.jpg", ".+-MULTIVIEW.jpg");
        Pattern urlPattern = Pattern.compile(urlRegex);

        List<String> urls = new ArrayList<String>();

        Matcher m = urlPattern.matcher(response);
        while (m.find()) {
            String picUrl = m.group(0);
            urls.add(picUrl);
        }

        initializeViewPager(urls);

    }

    private void initializeViewPager(List<String> urls) {

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ProductImageAdapter(this, urls));
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                pagerMoved = true;
                return false;
            }
        });

        findViewById(R.id.placeholderImage).setVisibility(View.GONE);
        mViewPager.setVisibility(View.VISIBLE);

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

}

class ProductImageAdapter extends PagerAdapter {

    private Context mContext;
    private List<String> imageUrls;

    public ProductImageAdapter(Context context) {
        mContext = context;
    }

    public ProductImageAdapter(Context context, List<String> urls) {
        mContext = context;
        imageUrls = urls;
    }



    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        LinearLayout layout =
                (LinearLayout) inflater.inflate(R.layout.product_pager_imageview, container, false);
        ImageView imageView =
                (ImageView) layout.findViewById(R.id.image);

        Picasso.with(mContext)
                .load(imageUrls.get(position))
                .placeholder(R.drawable.zappos_logo)
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

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }
}
