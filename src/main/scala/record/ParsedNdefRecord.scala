package org.proofcafe.nfcdemo.record

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

trait ParsedNdefRecord {

  /**
   * Returns a view to display this record.
   */
  def getView(activity: Activity, inflater: LayoutInflater, parent: ViewGroup, offset: Int): View
}
