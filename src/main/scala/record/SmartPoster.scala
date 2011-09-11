package org.proofcafe.nfcdemo.record

import android.app.Activity;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import org.proofcafe.nfcdemo.NdefMessageParser;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.nio.charset.Charset;
import scala.collection.immutable.HashMap

/**
 * A representation of an NFC Forum "Smart Poster".
 */
class SmartPoster private (uri: UriRecord, title: TextRecord, action: SmartPoster.RecommendedAction, typ: String) extends ParsedNdefRecord {
  import SmartPoster.RecommendedAction

  /**
   * NFC Forum Smart Poster Record Type Definition section 3.2.1.
   *
   * "The Title record for the service (there can be many of these in
   * different languages, but a language MUST NOT be repeated). This record is
   * optional."
   */
  private val mTitleRecord : TextRecord = title

  /**
   * NFC Forum Smart Poster Record Type Definition section 3.2.1.
   *
   * "The URI record. This is the core of the Smart Poster, and all other
   * records are just metadata about this record. There MUST be one URI record
   * and there MUST NOT be more than one."
   */
  private val mUriRecord : UriRecord = uri //Preconditions.checkNotNull(uri)

  /**
   * NFC Forum Smart Poster Record Type Definition section 3.2.1.
   *
   * "The Action record. This record describes how the service should be
   * treated. For example, the action may indicate that the device should save
   * the URI as a bookmark or open a browser. The Action record is optional.
   * If it does not exist, the device may decide what to do with the service.
   * If the action record exists, it should be treated as a strong suggestion;
   * the UI designer may ignore it, but doing so will induce a different user
   * experience from device to device."
   */
  private val mAction : RecommendedAction = action //Preconditions.checkNotNull(action)

  /**
   * NFC Forum Smart Poster Record Type Definition section 3.2.1.
   *
   * "The Type record. If the URI references an external entity (e.g., via a
   * URL), the Type record may be used to declare the MIME type of the entity.
   * This can be used to tell the mobile device what kind of an object it can
   * expect before it opens the connection. The Type record is optional."
   */
  private val mType : String = typ

  def getUriRecord() = mUriRecord

  /**
   * Returns the title of the smart poster. This may be {@code null}.
   */
  def getTitle() = mTitleRecord

  def getView(activity: Activity, inflater: LayoutInflater, parent: ViewGroup, offset: Int) = {
        if (mTitleRecord != null) {
            // Build a container to hold the title and the URI
            val container = new LinearLayout(activity);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
            container.addView(mTitleRecord.getView(activity, inflater, container, offset));
          //TODO inflater.inflate(R.layout.tag_divider, container);
            container.addView(mUriRecord.getView(activity, inflater, container, offset));
            container
        } else {
            // Just a URI, return a view for it directly
            mUriRecord.getView(activity, inflater, parent, offset)
        }
  }

}

object SmartPoster {
    def parse(record : NdefRecord) : SmartPoster = {
        // Preconditions.checkArgument(record.getTnf() == NdefRecord.TNF_WELL_KNOWN);
        // Preconditions.checkArgument(Arrays.equals(record.getType(), NdefRecord.RTD_SMART_POSTER));
        try {
            val subRecords = new NdefMessage(record.getPayload());
            parse(subRecords.getRecords());
        } catch {
          case (e: FormatException) =>
            throw new IllegalArgumentException(e);
        }
    }

    def parse(recordsRaw : Array[NdefRecord]) : SmartPoster = {
        try {
          val records = NdefMessageParser.getRecords(recordsRaw);
          val uri =
            records.filter(_.isInstanceOf[UriRecord]) match {
              case Nil => throw new NoSuchElementException()
              case List(uri:UriRecord) => uri
              case _ => throw new IllegalArgumentException()
            }
          val title = getFirstIfExists[TextRecord](records)
          val action = parseRecommendedAction(recordsRaw)
          val typ = parseType(recordsRaw);
          new SmartPoster(uri, title, action, typ);
        } catch {
          case (e: NoSuchElementException) =>
            throw new IllegalArgumentException(e);
        }
    }

  def isPoster(record : NdefRecord) : Boolean = {
    try {
      parse(record)
      true
    } catch {
      case (e: IllegalArgumentException) => false
    }
  }
  
  /**
   * Returns the first element of {@code elements} which is an instance of
   * {@code type}, or {@code null} if no such element exists.
   */
  private def getFirstIfExists[T >: Null](elements: List[_]): T =
      elements match {
        case (x:T) :: xs => x
        case _ => null
      }

  private def getByType(typ : Array[Byte], records : Array[NdefRecord]) : NdefRecord = {
        for (record <- records) {
            if (Arrays.equals(typ, record.getType())) {
                return record;
            }
        }
        return null;
  }

  private val ACTION_RECORD_TYPE = Array[Byte]('a', 'c', 't')

  private def parseRecommendedAction(records: Array[NdefRecord]) : RecommendedAction = {
    val record = getByType(ACTION_RECORD_TYPE, records)
    if (record == null) {
      return UNKNOWN
    }
    val action = record.getPayload().apply(0);
    LOOKUP.get(action) match {
      case Some(a) => a
      case None => UNKNOWN
    }
  }

  private val TYPE_TYPE = Array[Byte]('t')

  private def parseType(records : Array[NdefRecord]) : String = {
        val typ = getByType(TYPE_TYPE, records);
        if (typ == null) {
            return null;
        }
        return new String(typ.getPayload(), Charset.forName("UTF_8"));
  }

  class RecommendedAction(private val mAction : Byte) {
    private def getByte() = mAction
  }
  case object UNKNOWN extends RecommendedAction(-1)
  case object DO_ACTION extends RecommendedAction(0)
  case object SAVE_FOR_LATER extends RecommendedAction(1)
  case object OPEN_FOR_EDITING extends RecommendedAction(2)

  def LOOKUP = HashMap(
    -1 -> UNKNOWN,
    0  -> DO_ACTION,
    1  -> SAVE_FOR_LATER,
    2  -> OPEN_FOR_EDITING
  )
}
