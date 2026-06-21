package android.print

import android.os.CancellationSignal
import android.os.ParcelFileDescriptor

object PrintHelper {
    fun runWrite(
        adapter: PrintDocumentAdapter,
        attributes: PrintAttributes,
        pfd: ParcelFileDescriptor,
        onComplete: () -> Unit,
        onFailed: (String?) -> Unit
    ) {
        adapter.onLayout(
            null,
            attributes,
            CancellationSignal(),
            object : PrintDocumentAdapter.LayoutResultCallback() {
                override fun onLayoutFinished(info: PrintDocumentInfo?, changed: Boolean) {
                    adapter.onWrite(
                        arrayOf(PageRange.ALL_PAGES),
                        pfd,
                        CancellationSignal(),
                        object : PrintDocumentAdapter.WriteResultCallback() {
                            override fun onWriteFinished(pages: Array<out PageRange>?) {
                                onComplete()
                            }

                            override fun onWriteFailed(error: CharSequence?) {
                                onFailed(error?.toString())
                            }

                            override fun onWriteCancelled() {
                                onFailed("Cancelled")
                            }
                        }
                    )
                }

                override fun onLayoutFailed(error: CharSequence?) {
                    onFailed(error?.toString())
                }

                override fun onLayoutCancelled() {
                    onFailed("Cancelled")
                }
            },
            null
        )
    }
}

