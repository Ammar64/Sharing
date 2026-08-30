package com.ammar.sharing.custom.glide

import android.os.ParcelFileDescriptor
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import java.io.IOException
import java.io.InputStream


class ParcelFileDescriptorLoader : ModelLoader<ParcelFileDescriptor?, InputStream?> {
    override fun buildLoadData(
        model: ParcelFileDescriptor,
        width: Int,
        height: Int,
        options: Options
    ): LoadData<InputStream?> {
        return LoadData<InputStream?>(ObjectKey(model), ParcelFileDescriptorFetcher(model))
    }

    override fun handles(model: ParcelFileDescriptor): Boolean {
        return true
    }

    class Factory : ModelLoaderFactory<ParcelFileDescriptor?, InputStream?> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<ParcelFileDescriptor?, InputStream?> {
            return ParcelFileDescriptorLoader()
        }

        override fun teardown() {
        }
    }


    internal class ParcelFileDescriptorFetcher(private val pfd: ParcelFileDescriptor?) :
        DataFetcher<InputStream?> {
        private var stream: InputStream? = null

        override fun loadData(
            priority: Priority,
            callback: DataFetcher.DataCallback<in InputStream?>
        ) {
            try {
                stream = ParcelFileDescriptor.AutoCloseInputStream(pfd)
                callback.onDataReady(stream)
            } catch (e: Exception) {
                callback.onLoadFailed(e)
            }
        }

        override fun cleanup() {
            try {
                if (stream != null) stream!!.close()
            } catch (_: IOException) {
            }
        }

        override fun cancel() {
        }

        @Suppress("UNCHECKED_CAST")
        override fun getDataClass(): Class<InputStream?> {
            return InputStream::class.java as Class<InputStream?>
        }

        override fun getDataSource(): DataSource {
            return DataSource.LOCAL
        }
    }
}