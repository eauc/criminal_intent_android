package com.bignerdranch.android.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

public class CrimeListActivity extends SingleFragmentActivity
        implements CrimeFragment.Callbacks {
    private static final String TAG = "CrimeListActivity";

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = CrimePagerActivity.newIntent(this, crime.getId());
            startActivity(intent);
        } else {
            Fragment crimeFragment = CrimeFragment.newInstance(crime.getId());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, crimeFragment)
                    .commit();
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }

    @Override
    public void onCrimeDeleted(Crime crime) {
        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
        Log.d(TAG, "onCrimeDeleted()");
        CrimeFragment crimeFragment = (CrimeFragment) getSupportFragmentManager()
                .findFragmentById(R.id.detail_fragment_container);
        if (crimeFragment != null) {
            Crime currentCrimeDetail = crimeFragment.getCrime();
            Log.d(TAG, "onCrimeDeleted() detail " + currentCrimeDetail.getId() + ", " + crime.getId());
            if (currentCrimeDetail.getId().compareTo(crime.getId()) != 0) {
                return;
            }

            Log.d(TAG, "onCrimeDeleted() remove");
            getSupportFragmentManager().beginTransaction()
                    .remove(crimeFragment)
                    .commit();
        }
    }
}
