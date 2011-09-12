package org.proofcafe.nfcdemo.simulator

import android.app.ListActivity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.util.Log

import org.proofcafe.nfcdemo.util.Preconditions

import java.nio.charset.Charset;
import java.util.Locale;

/**
 * A activity that launches tags as if they had been scanned.
 */
class FakeTagsActivity extends ListActivity {
  import FakeTagsActivity.TagDescription

  val TAG = "FakeTagsActivity";

  val UID = Array[Byte](0x05, 0x00, 0x03, 0x08);

  var mAdapter : ArrayAdapter[TagDescription] = null;

  override def onCreate(savedState : Bundle) {
    super.onCreate(savedState)
    val adapter = new ArrayAdapter[TagDescription](this, android.R.layout.simple_list_item_1, android.R.id.text1)
    
    adapter.add(new TagDescription(
      "Broadcast NFC Text Tag",
      MockNdefMessages.ENGLISH_PLAIN_TEXT
    ))
    adapter.add(new TagDescription(
      "Broadcast NFC SmartPoster URL & text",
      MockNdefMessages.SMART_POSTER_URL_AND_TEXT
    ))
    adapter.add(new TagDescription(
      "Broadcast NFC SmartPoster URL",
      MockNdefMessages.SMART_POSTER_URL_NO_TEXT
    ))
    setListAdapter(adapter)
    mAdapter = adapter
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val description = mAdapter.getItem(position)
    val intent = new Intent(NfcAdapter.ACTION_TAG_DISCOVERED)
    intent.putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, description.msgs)
    startActivity(intent)
  }
}

object FakeTagsActivity {
  def newTextRecord(text : String, locale : Locale, encodeInUtf8 : Boolean) = {
    Preconditions.checkNotNull(text);
    Preconditions.checkNotNull(locale);
    val langBytes = locale.getLanguage().getBytes(Charset.forName("US_ASCII"));
    val utfEncoding = if (encodeInUtf8)  Charset.forName("UTF_8") else Charset.forName("UTF-16")
    val textBytes = text.getBytes(utfEncoding);
    val utfBit = if (encodeInUtf8)  0 else (1 << 7)
    val status = (utfBit + langBytes.length).toChar
    val data = Array[Byte](status.toByte) ++ langBytes ++ textBytes
    new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, Array[Byte](), data);
  }

  def newMimeRecord(typ : String, data : Array[Byte]) = {
    Preconditions.checkNotNull(typ);
    Preconditions.checkNotNull(data);
    val typeBytes = typ.getBytes(Charset.forName("US_ASCII"));
    new NdefRecord(NdefRecord.TNF_MIME_MEDIA, typeBytes, Array[Byte](), data);
  }

  class TagDescription(val title : String, bytes : Array[Byte]) {

    val msgs : Array[NdefMessage] = try {
      Array(new NdefMessage(bytes));
    } catch {
      case e:Exception => throw new RuntimeException("Failed to create tag description", e);
    }

    override def toString() = title
  }
}
