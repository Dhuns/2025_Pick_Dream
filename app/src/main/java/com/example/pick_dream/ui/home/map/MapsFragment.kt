package com.example.pick_dream.ui.home.map

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.pick_dream.R
import com.example.pick_dream.databinding.FragmentMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment(), OnMapReadyCallback {

    private var map: GoogleMap? = null
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    data class Place(
        val name: String,
        val description: String,
        val imageResList: List<Int>,
        val rating: Float,
        val availableRooms: Int,
        val latLng: LatLng
    )

    private val places = listOf(
        Place(
            name = "덕문관 (5강의동)",
            description = "예약 가능 강의실 : N개",
            imageResList = listOf(R.drawable.sample_room, R.drawable.p_5kang), // drawable에 이미지 추가 필요
            rating = 4.5f,
            availableRooms = 5,
            latLng = LatLng(37.2999561, 127.0367820)
        ),
        Place(
            name = "집현관 (7강의동)",
            description = "예약 가능 강의실 : 3개",
            imageResList = listOf(R.drawable.p_7kang),
            rating = 4.0f,
            availableRooms = 3,
            latLng = LatLng(37.301269, 127.038786)
        ),
        Place(
            name = "육영관 (8강의동)",
            description = "예약 가능 강의실 : 2개",
            imageResList = listOf(R.drawable.p_8kang),
            rating = 3.8f,
            availableRooms = 2,
            latLng = LatLng(37.300731, 127.039265)
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.infoCard.visibility = View.GONE
        setupMap()
        setupUI()
        setupBackPress()
        setupSearchAutoComplete()

        val placeNames = places.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, placeNames)
        val searchInput = view.findViewById<AutoCompleteTextView>(R.id.searchInput)
        searchInput.setAdapter(adapter)

        searchInput.setOnItemClickListener { _, _, position, _ ->
            val selectedPlaceName = adapter.getItem(position)
            val selectedPlace = places.find { it.name == selectedPlaceName }
            if (selectedPlace != null) {
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedPlace.latLng, 16f))
                showPlaceInfo(selectedPlace)
            }
        }
    }

    private fun setupMap() {
        try {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as? SupportMapFragment
            if (mapFragment != null) {
                mapFragment.getMapAsync(this)
            } else {
                Toast.makeText(context, "지도를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "지도 초기화 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        try {
            map = googleMap
            places.forEach { place ->
                val marker = map?.addMarker(
                    MarkerOptions()
                        .position(place.latLng)
                        .title(place.name)
                )
                marker?.tag = place
            }
            map?.setOnMarkerClickListener { marker ->
                val place = marker.tag as? Place
                if (place != null) {
                    showPlaceInfo(place)
                }
                true
            }
            map?.setOnMapClickListener {
                if (binding.infoCard.visibility == View.VISIBLE) {
                    binding.infoCard
                        .animate()
                        .translationY(binding.infoCard.height.toFloat())
                        .setDuration(300)
                        .withEndAction {
                            binding.infoCard.visibility = View.GONE
                            binding.infoCard.translationY = 0f
                        }
                        .start()
                }
            }
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(places[0].latLng, 16f))
        } catch (e: Exception) {
            Toast.makeText(context, "지도 설정 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPlaceInfo(place: Place) {
        binding.infoCard.visibility = View.VISIBLE
        binding.placeImage.setImageResource(place.imageResList.first())
        binding.placeName.text = place.name
        binding.placeDesc.text = place.description
        binding.placeRating.rating = place.rating
        binding.placeImage.setOnClickListener {
            ImagePreviewDialogFragment
                .newInstance(ArrayList(place.imageResList))
                .show(childFragmentManager, "imgm_preview")
        }
        val imageList: ArrayList<Int> = ArrayList(place.imageResList)
        binding.placeImage.setOnClickListener {
            ImagePreviewDialogFragment.newInstance(imageList)
                .show(childFragmentManager, "img_preview")
        }
        binding.btnReserve.setOnClickListener {
            val buildingDetail = place.name.substringAfter("(").substringBefore(")")
            val buildingName = place.name.substringBefore(" (")
            
            val action = MapsFragmentDirections
                .actionMapsFragmentToLectureRoomSelectionFragment(
                    buildingName = buildingName,
                    buildingDetail = buildingDetail
                )
            findNavController().navigate(action)
        }
    }

    private fun setupUI() {
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.GONE
        binding.toolbarTitle.text = getString(R.string.map)
        binding.btnBack.setOnClickListener {
            navigateToHome()
        }
    }

    private fun setupBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateToHome()
                }
            }
        )
    }

    private fun setupSearchAutoComplete() {
        val placeNames = places.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, placeNames)
        binding.searchInput.setAdapter(adapter)
        binding.searchInput.threshold = 1
        binding.searchInput.addTextChangedListener {
            binding.searchInput.showDropDown()
        }
        binding.searchInput.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position) as String
            val selectedPlace = places.find { it.name == selectedName }
            selectedPlace?.let {
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(it.latLng, 17f))
                showPlaceInfo(it)
            }
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(
            R.id.homeFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, false)
                .build()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
        map = null
        _binding = null
    }
}