#ILoveZappos

ILoveZappos is a Android application developed as part a challenge for the Zappos Android Developement Summer Internship Program. The application features a product search and product page allowing the user to search for a desired product and displaying the first result from the search.  As per the requirements, the application also features **databinding** to drive the product page and a **Floating Action Button** that animates upon click to indicate the user has added the item to her cart. 

Additionally, the application features bonus material including a Material Design. More specifically, it features a CoordinatorLayout that allows content to scroll while pinning important material at the top. Finally, it also uses **retrofit** to handle API requests. Furthermore, it uses deeplinking and a share intent to allow sharing of products. For example, the link ```zappos://product/id=8619473,term=Tanjun``` opens up the product page for the [Nike Tanjun](http://www.zappos.com/p/nike-tanjun-black-white/product/8619473/color/151).

Ignoring core value #10, the app also goes beyond to make a request to the Zappos website to retrieve more information on the product. More specifically, it uses Volley to pull the source code for the respective website page for the product. It then finds the description in the source code and formats and displays the text on the app. In addition, it finds all the product images and uses Picasso to display them in the form of an autoscrolling ViewPager at the top application. 

###Screenshots
![alt tag](https://github.com/vkhemlani96/ILoveZappos/blob/master/screenshots/1.jpg)
![alt tag](https://github.com/vkhemlani96/ILoveZappos/blob/master/screenshots/2.png)
![alt tag](https://github.com/vkhemlani96/ILoveZappos/blob/master/screenshots/3.png)
![alt tag](https://github.com/vkhemlani96/ILoveZappos/blob/master/screenshots/4.png)

###Libraries Used
- [Retrofit](https://square.github.io/retrofit/)
- [Picasso](http://square.github.io/picasso/)
- [CircleIndicator](https://github.com/ongakuer/CircleIndicator)
