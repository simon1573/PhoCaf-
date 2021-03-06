package se.tuxflux.phocafe.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.prof.rssparser.Article
import com.prof.rssparser.ObservableRssFeed
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import se.tuxflux.phocafe.R
import se.tuxflux.phocafe.adapter.ContentAdapter
import se.tuxflux.phocafe.toast
import timber.log.Timber




class MainActivity : AppCompatActivity() {

    private var disposables: CompositeDisposable = CompositeDisposable()

    private val rssRepo by lazy {
        ObservableRssFeed("http://www.foocafe.org/malmoe/events.rss")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        val adapter = ContentAdapter()
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        recycler.adapter = adapter


        val disposable = adapter.clickEvent
                .subscribe({ article ->
                    baseContext.toast("Clicked on " + article.link)
                })

        disposables.add(disposable)

        rssRepo.articles
                .doOnError { e -> Timber.e(e) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Article> {
                    override fun onSubscribe(d: Disposable) {
                        disposables.add(d)
                    }

                    override fun onNext(article: Article) {
                        adapter.add(article)
                    }

                    override fun onError(e: Throwable) {
                        Timber.e(e)
                    }

                    override fun onComplete() {
                        Timber.i("Done loading RSS")
                    }
                })

    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }
}
