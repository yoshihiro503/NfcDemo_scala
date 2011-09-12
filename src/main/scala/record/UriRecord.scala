package org.proofcafe.nfcdemo.record

import android.app.Activity;
import android.net.Uri;
import android.nfc.NdefRecord;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log

import org.proofcafe.nfcdemo.util.Preconditions

import java.nio.charset.Charset;
import java.util.Arrays;
import scala.collection.immutable.HashMap

/**
 * A parsed record containing a Uri.
 */
class UriRecord(uri : Uri) extends ParsedNdefRecord {

  val TAG = "UriRecord"
  val RECORD_TYPE = "UriRecord"

  private val mUri = Preconditions.checkNotNull(uri);

  def getView(activity : Activity, inflater : LayoutInflater, parent : ViewGroup, offset : Int) = {
    val text : TextView = new TextView(activity) //TODO inflater.inflate("Text", parent, false);
    text.setText(mUri.toString())
    text
  }

  def getUri() = mUri
}


object UriRecord {

  /**
   * NFC Forum "URI Record Type Definition"
   *
   * This is a mapping of "URI Identifier Codes" to URI string prefixes,
   * per section 3.2.2 of the NFC Forum URI Record Type Definition document.
   */
  val URI_PREFIX_MAP : Map[Byte, String] = HashMap(
            0x00 -> "",
            0x01 -> "http://www.",
            0x02 -> "https://www.",
            0x03 -> "http://",
            0x04 -> "https://",
            0x05 -> "tel:",
            0x06 -> "mailto:",
            0x07 -> "ftp://anonymous:anonymous@",
            0x08 -> "ftp://ftp.",
            0x09 -> "ftps://",
            0x0A -> "sftp://",
            0x0B -> "smb://",
            0x0C -> "nfs://",
            0x0D -> "ftp://",
            0x0E -> "dav://",
            0x0F -> "news:",
            0x10 -> "telnet://",
            0x11 -> "imap:",
            0x12 -> "rtsp://",
            0x13 -> "urn:",
            0x14 -> "pop:",
            0x15 -> "sip:",
            0x16 -> "sips:",
            0x17 -> "tftp:",
            0x18 -> "btspp://",
            0x19 -> "btl2cap://",
            0x1A -> "btgoep://",
            0x1B -> "tcpobex://",
            0x1C -> "irdaobex://",
            0x1D -> "file://",
            0x1E -> "urn:epc:id:",
            0x1F -> "urn:epc:tag:",
            0x20 -> "urn:epc:pat:",
            0x21 -> "urn:epc:raw:",
            0x22 -> "urn:epc:",
            0x23 -> "urn:nfc:"
  ).map{ case(k,v) => (k.toByte, v) }

  
  /**
   * Convert {@link android.nfc.NdefRecord} into a {@link android.net.Uri}.
   * This will handle both TNF_WELL_KNOWN / RTD_URI and TNF_ABSOLUTE_URI.
   *
   * @throws IllegalArgumentException if the NdefRecord is not a record
   *         containing a URI.
   */
  def parse(record : NdefRecord) = {
    val tnf = record.getTnf()
    if (tnf == NdefRecord.TNF_WELL_KNOWN) {
      parseWellKnown(record)
    } else if (tnf == NdefRecord.TNF_ABSOLUTE_URI) {
      parseAbsolute(record)
    } else {
      throw new IllegalArgumentException("Unknown TNF " + tnf);
    }
  }

    /** Parse and absolute URI record */
    private def parseAbsolute(record : NdefRecord) = {
      val payload = record.getPayload()
      val uri = Uri.parse(new String(payload, Charset.forName("UTF-8")))
      new UriRecord(uri)
    }

  /** Parse an well known URI record */
  private def parseWellKnown(record : NdefRecord) = {
    Preconditions.checkArgument(Arrays.equals(record.getType(), NdefRecord.RTD_URI));
    val payload = record.getPayload()
    /*
     * payload[0] contains the URI Identifier Code, per the
     * NFC Forum "URI Record Type Definition" section 3.2.2.
     *
     * payload[1]...payload[payload.length - 1] contains the rest of
     * the URI.
     */
    val prefix = URI_PREFIX_MAP(payload(0));
    val fullUri =
      prefix.getBytes(Charset.forName("UTF-8")) ++ Arrays.copyOfRange(payload, 1, payload.length);
    val uri = Uri.parse(new String(fullUri, Charset.forName("UTF-8")));
    new UriRecord(uri)
  }

  def isUri(record : NdefRecord) =
    try {
      parse(record);
      true;
    } catch {
      case (e:IllegalArgumentException) => false
    }

  val EMPTY = Array[Byte]()
}
