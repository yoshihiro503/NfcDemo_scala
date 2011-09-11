package org.proofcafe.nfcdemo.record

import android.app.Activity;
import android.nfc.NdefRecord;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * An NFC Text Record
 */
class TextRecord private (languageCode : String, text : String) extends ParsedNdefRecord {

  /** ISO/IANA language code */
  private val mLanguageCode = languageCode //Preconditions.checkNotNull(languageCode);

  private val mText = text //Preconditions.checkNotNull(text);

  def getView(activity : Activity, inflater : LayoutInflater, parent : ViewGroup, offset : Int) = {
    val text : TextView = new TextView(activity) //TODO (TextView) inflater.inflate(R.layout.tag_text, parent, false);
    text.setText(mText)
    text
  }
  
  def getText() = mText

  /**
   * Returns the ISO/IANA language code associated with this text element.
   */
  def getLanguageCode() = mLanguageCode


}


object TextRecord {

  // TODO: deal with text fields which span multiple NdefRecords
  def parse(record : NdefRecord) : TextRecord = {
    // Preconditions.checkArgument(record.getTnf() == NdefRecord.TNF_WELL_KNOWN);
    // Preconditions.checkArgument(Arrays.equals(record.getType(), NdefRecord.RTD_TEXT));
    try {
      val payload = record.getPayload();
      /*
       * payload[0] contains the "Status Byte Encodings" field, per the
       * NFC Forum "Text Record Type Definition" section 3.2.1.
       *
       * bit7 is the Text Encoding Field.
       *
       * if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1):
       * The text is encoded in UTF16
       *
       * Bit_6 is reserved for future use and must be set to zero.
       *
       * Bits 5 to 0 are the length of the IANA language code.
       */
      val textEncoding = if ((payload(0) & 0200) == 0) "UTF-8" else "UTF-16"
      val languageCodeLength = payload(0) & 0077;
      val languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
      val text =
        new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
      new TextRecord(languageCode, text);
    } catch {
      case (e: UnsupportedEncodingException) =>
        // should never happen unless we get a malformed tag.
        throw new IllegalArgumentException(e);
    }
  }

  def isText(record : NdefRecord) =
    try {
      parse(record)
      true
    } catch {
      case (e:IllegalArgumentException) => false
    }
}
