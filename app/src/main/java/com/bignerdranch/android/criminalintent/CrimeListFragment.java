package com.bignerdranch.android.criminalintent;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CrimeListFragment extends Fragment {
    private static final String TAG = "CrimeListFragment";
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private static final int REQUEST_READ_CONTACTS = 1;

    private TextView mNoCrimesTextView;
    private Button mNoCrimesButton;
    private RecyclerView mCrimeListRecyclerView;
    private CrimeAdapter mCrimeAdapter;
    private boolean mSubtitleVisible;

    private Callbacks mCallbacks;

    public interface Callbacks {
        void onCrimeSelected(Crime crime);
        void onCrimeDeleted(Crime crime);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedStateInstance) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        if (savedStateInstance != null) {
            mSubtitleVisible = savedStateInstance.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        mNoCrimesTextView = (TextView)view.findViewById(R.id.no_crimes_textview);
        mNoCrimesButton = (Button)view.findViewById(R.id.no_crimes_button);
        mNoCrimesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newCrime();
            }
        });

        mCrimeListRecyclerView = (RecyclerView)view.findViewById(R.id.crime_recycler_view);
        mCrimeListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
            {
                newCrime();
                return true;
            }
            case R.id.show_subtitle:
            {
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void newCrime() {
        Crime crime = new Crime();
        CrimeLab.get(getActivity()).addCrime(crime);
        updateUI();
        mCallbacks.onCrimeSelected(crime);
    }

    public void updateUI() {
        CrimeLab lab = CrimeLab.get(getActivity());
        List<Crime> crimes = lab.getCrimes();

        if(mCrimeAdapter == null) {
            mCrimeAdapter = new CrimeAdapter(crimes);
            mCrimeListRecyclerView.setAdapter(mCrimeAdapter);
            ItemTouchHelper.Callback touchHelperCallback = new ItemTouchHelperAdapter(mCrimeAdapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(touchHelperCallback);
            touchHelper.attachToRecyclerView(mCrimeListRecyclerView);
        } else {
            mCrimeAdapter.setCrimes(crimes);
            mCrimeAdapter.notifyDataSetChanged();
        }

        if(CrimeLab.get(getActivity()).getCrimes().isEmpty()) {
            mNoCrimesTextView.setVisibility(View.VISIBLE);
            mNoCrimesButton.setVisibility(View.VISIBLE);
        } else {
            mNoCrimesTextView.setVisibility(View.GONE);
            mNoCrimesButton.setVisibility(View.GONE);
        }

        updateSubtitle();
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = mSubtitleVisible ? getString(R.string.subtitle_format, crimeCount) : null;

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;

        private Crime mCrime;

        public CrimeHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_crime_title_textview);
            mDateTextView = (TextView) itemView.findViewById(R.id.list_item_crime_date_textview);
            mSolvedImageView = (ImageView) itemView.findViewById(R.id.list_item_crime_solved_imageview);
        }

        public void bindCrime(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(crime.getTitle());
            mDateTextView.setText(crime.getDate().toString());
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View v) {
            mCallbacks.onCrimeSelected(mCrime);
        }
    }

    private interface ItemTouchHelperCallbackAdapter {
        void onSwiped(int index);
    }

    private class ItemTouchHelperAdapter extends ItemTouchHelper.Callback {
        private ItemTouchHelperCallbackAdapter mAdapter;

        public ItemTouchHelperAdapter(ItemTouchHelperCallbackAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public int getMovementFlags(RecyclerView v, RecyclerView.ViewHolder holder) {
            return makeMovementFlags(0, ItemTouchHelper.END);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder holder, int direction) {
            int index = holder.getAdapterPosition();
            Log.d(TAG, "onSwiped<Callback> " + index);
            mAdapter.onSwiped(index);
        }

        @Override
        public boolean onMove(RecyclerView v, RecyclerView.ViewHolder from, RecyclerView.ViewHolder to) {
            return false;
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder>
            implements ItemTouchHelperCallbackAdapter {
        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_crime, parent, false);
            return new CrimeHolder(view);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bindCrime(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public void onSwiped(int index) {
            Crime crime = mCrimes.get(index);
            CrimeLab.get(getActivity()).deleteCrime(crime.getId());
            Log.d(TAG, "onSwiped() " + crime.getId());
            mCallbacks.onCrimeDeleted(crime);
        }
    }
}
