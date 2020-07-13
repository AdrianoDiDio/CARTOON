package com.adriano.cartoon.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.adriano.cartoon.R;
import com.adriano.cartoon.restclients.RestMapClient;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;

import java.util.ArrayList;

public class PlacesAutoCompleteAdapter extends RecyclerView.Adapter<PlacesAutoCompleteAdapter.PredictionHolder> implements Filterable {
    private ArrayList<Place> resultList = new ArrayList<>();
    private Context context;
    private CharacterStyle STYLE_BOLD;
    private CharacterStyle STYLE_NORMAL;
//    private final PlacesClient placesClient;
    private PlacesAutoCompleteClickListener clickListener;
    private AutocompleteSessionToken autocompleteSessionToken;
    private RestMapClient restMapClient;

    public PlacesAutoCompleteAdapter(RestMapClient restMapClient, Context context) {
        this.context = context;
        STYLE_BOLD = new StyleSpan(Typeface.BOLD);
        STYLE_NORMAL = new StyleSpan(Typeface.NORMAL);
        this.restMapClient = restMapClient;
//        placesClient = com.google.android.libraries.places.api.Places.createClient(context);
//        autocompleteSessionToken = AutocompleteSessionToken.newInstance();
    }

    public void setClickListener(PlacesAutoCompleteClickListener clickListener) {
        this.clickListener = clickListener;
    }


    /**
     * Returns the filter for the current set of autocomplete results.
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<Place> placeArrayList = null;
                // Skip the autocomplete query if no constraints are given.
                if (constraint != null && constraint.length() != 0 ) {
                    // Query the autocomplete API for the (constraint) search string.
                    placeArrayList = getPredictions(constraint);
//                    if (resultList != null) {
//                        // The API successfully returned results.
//                    }
//                    results.values = resultList;
//                    results.count = resultList.size();
                } else {
                    placeArrayList = resultList;
                }
                results.values = placeArrayList;
                results.count = placeArrayList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                resultList = (ArrayList<Place>) results.values;
                notifyDataSetChanged();
            }
        };
    }


    /*
    * NOTE(Adriano):
    * restMapClient.autocomplete is a Synchronous call that must not be called
    * on the main thread, in this instance we are running it on a worker thread
    * due to the way Filter works.
    * */
    private ArrayList<Place> getPredictions(CharSequence constraint) {
        ArrayList<Place> placeArrayList;
        placeArrayList = restMapClient.autocomplete(constraint.toString());
        return placeArrayList;
//        final ArrayList<Place> placeArrayList = new ArrayList<>();
//        AutoCompleteAsyncTask autoCompleteBlockingTask;
//        autoCompleteBlockingTask = new AutoCompleteAsyncTask(constraint.toString());
//        autoCompleteBlockingTask.execute();
//        try {
//            ArrayList<Place> result = autoCompleteBlockingTask.get(60, TimeUnit.SECONDS);
//            if( result != null ) {
//                for( Place iterator : result ) {
//                    placeArrayList.add(iterator);
//                }
////                placeArrayList.addAll(result);
//            }
//
//        } catch (ExecutionException | InterruptedException | TimeoutException e) {
//            e.printStackTrace();
//        }
//        return placeArrayList;
//        Task<String> getAutocomplete;
//
//        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
//                .setSessionToken(autocompleteSessionToken)
//                .setQuery(constraint.toString())
//                .build();
//
//        Task<FindAutocompletePredictionsResponse> autocompletePredictions = placesClient.findAutocompletePredictions(request);
//
//        //TODO:Customize this TimeOut...
//        try {
//            Tasks.await(autocompletePredictions, 60, TimeUnit.SECONDS);
//        } catch (ExecutionException | InterruptedException | TimeoutException e) {
//            e.printStackTrace();
//        }
//
//        if (autocompletePredictions.isSuccessful()) {
//            FindAutocompletePredictionsResponse findAutocompletePredictionsResponse =
//                    autocompletePredictions.getResult();
//            if (findAutocompletePredictionsResponse != null)
//                for (AutocompletePrediction prediction : findAutocompletePredictionsResponse.getAutocompletePredictions()) {
//                    Timber.d("Place id is: " + prediction.getPlaceId());
//                    resultList.add(new PlaceAutoCompleteHolder(prediction.getPlaceId(),
//                            prediction.getPrimaryText(STYLE_NORMAL).toString(),
//                            prediction.getFullText(STYLE_BOLD).toString()));
//                }
//
//            return resultList;
//        } else {
//            return resultList;
//        }

    }

    @NonNull
    @Override
    public PredictionHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View convertView = layoutInflater.inflate(R.layout.place_recycler_item_layout, viewGroup,
                false);
        return new PredictionHolder(convertView);
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull PredictionHolder mPredictionHolder, int i) {
        mPredictionHolder.address.setText(resultList.get(i).address);
        mPredictionHolder.area.setText(resultList.get(i).fullAddress);
    }

    public Place getItem(int position) {
        return resultList.get(position);
    }

    public void clear() {
        int size = resultList.size();
        resultList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public class PredictionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView address, area;
        private CardView mRow;

        PredictionHolder(View itemView) {
            super(itemView);
            area = itemView.findViewById(R.id.place_area);
            address = itemView.findViewById(R.id.place_address);
            mRow = itemView.findViewById(R.id.place_item_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Place item = resultList.get(getAdapterPosition());
            if (v.getId() == R.id.place_item_view) {
                clickListener.onAutoCompleteResultClick(item);
//                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
//                FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();
//                placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
//                    @Override
//                    public void onSuccess(FetchPlaceResponse response) {
//                        Place place = response.getPlace();
//                        clickListener.onAutoCompleteResultClick(place);
//                    }
//                }).addOnFailureListener(exception -> {
//                    if (exception instanceof ApiException) {
//                        Timber.d(exception);
//                    }
//                });
//                autocompleteSessionToken = AutocompleteSessionToken.newInstance();
            }
        }
    }
}
