package org.proofcafe.nfcdemo

import _root_.android.app.Activity
import _root_.android.os.Bundle
import _root_.android.widget.TextView

import android.content.Intent;
import android.widget.LinearLayout
import android.widget.ScrollView
import android.os.Parcelable;
import android.nfc.NfcAdapter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.util.Log;
import android.view.LayoutInflater;

class TagViewer extends Activity {
  val TAG = "ViewTag";

  /**
   * This activity will finish itself in this amount of time if the user
   * doesn't do anything.
   */
  val ACTIVITY_TIMEOUT_MS = 1 * 1000;

  var mTitle : TextView = null
  var mTagContent : LinearLayout = null

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
    val self = this
    mTitle = new TextView(self)
    mTagContent = new LinearLayout(self)

    setContentView(new LinearLayout(self) {
      setOrientation(LinearLayout.VERTICAL)
      addView(mTitle)
      addView(new ScrollView(self) {
        addView(mTagContent)
      })
    })

    resolveIntent(getIntent())
  }

  private def resolveIntent(intent : Intent) = {
    // Parse the intent
    intent.getAction() match {
      case NfcAdapter.ACTION_TAG_DISCOVERED =>
            // When a tag is discovered we send it to the service to be save. We
            // include a PendingIntent for the service to call back onto. This
            // will cause this activity to be restarted with onNewIntent(). At
            // that time we read it from the database and view it.
        val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        val msgs =
          if (rawMsgs != null) {
            rawMsgs.map(_.asInstanceOf[NdefMessage])
          } else {
            // Unknown tag type
            var empty = Array[Byte]()
            val record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
            val msg = new NdefMessage(Array(record))
            Array(msg)
          }
        // Setup the views
        setTitle("New tag collected")
        buildTagViews(msgs)
      case _ =>
        Log.e(TAG, "Unknown intent " + intent)
        finish()
    }
  }

  private def buildTagViews(msgs: Array[NdefMessage]) = {
    if (msgs != null && msgs.length != 0) {
      val inflater = LayoutInflater.from(this)
      val content = mTagContent
      // Clear out any old views in the content area, for example if you scan
      // two tags in a row.
      content.removeAllViews()
      // Parse the first message in the list
      // Build views for all of the sub records
      val records = NdefMessageParser.parse(msgs(0))
      records.zipWithIndex.foreach {
        case (record, i) =>
          content.addView(record.getView(this, inflater, content, i))
        //TODO inflater.inflate(R.layout.tag_divider, content, true);
      }
    }
  }

    override def onNewIntent(intent : Intent) = {
      setIntent(intent)
      resolveIntent(intent)
    }

    override def setTitle(title : CharSequence) = {
      mTitle.setText(title);
    }
}
