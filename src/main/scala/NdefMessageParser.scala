package org.proofcafe.nfcdemo

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import org.proofcafe.nfcdemo.record.ParsedNdefRecord;
import org.proofcafe.nfcdemo.record.SmartPoster;
import org.proofcafe.nfcdemo.record.TextRecord;
import org.proofcafe.nfcdemo.record.UriRecord;

/**
 * Utility class for creating {@link ParsedNdefMessage}s.
 */
object NdefMessageParser {

  /** Parse an NdefMessage */
  def parse(message : NdefMessage) = getRecords(message.getRecords())

  def getRecords(records : Array[NdefRecord]): List[ParsedNdefRecord] =
    records.toList.flatMap {
      record =>
        if (UriRecord.isUri(record)) {
          List(UriRecord.parse(record));
        } else if (TextRecord.isText(record)) {
          List(TextRecord.parse(record));
        } else if (SmartPoster.isPoster(record)) {
          List(SmartPoster.parse(record))
        } else {
          Nil
        }
    }
}

