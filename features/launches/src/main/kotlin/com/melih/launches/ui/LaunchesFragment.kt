package com.melih.launches.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.melih.abstractions.deliverable.State
import com.melih.core.actions.openDetail
import com.melih.core.base.lifecycle.BaseDaggerFragment
import com.melih.core.extensions.observe
import com.melih.interactors.error.PersistenceEmptyError
import com.melih.launches.R
import com.melih.launches.data.LaunchItem
import com.melih.launches.databinding.LaunchesBinding
import com.melih.launches.ui.adapters.LaunchesAdapter
import com.melih.launches.ui.vm.LaunchesViewModel

class LaunchesFragment : BaseDaggerFragment<LaunchesBinding>(), SwipeRefreshLayout.OnRefreshListener {

    //region Properties

    private val viewModel by viewModels<LaunchesViewModel> { viewModelFactory }

    private val launchesAdapter = LaunchesAdapter(::onItemSelected)
    //endregion

    //region Lifecyle

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //setHasOptionsMenu(true)

        binding.rocketList.adapter = launchesAdapter
        binding.swipeRefreshLayout.setOnRefreshListener(this)

        observeDataChanges()
    }

    override fun onResume() {
        super.onResume()

        // Workaround for SwipeRefreshLayout leak -> https://issuetracker.google.com/issues/136153683
        binding.swipeRefreshLayout.isEnabled = true
    }

    override fun onPause() {
        super.onPause()

        // Workaround for SwipeRefreshLayout leak -> https://issuetracker.google.com/issues/136153683
        binding.swipeRefreshLayout.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rocketList.adapter = null
    }
    //endregion

    //region Functions

    private fun observeDataChanges() {

        // Observing state to show loading
        observe(viewModel.stateData) {
            binding.swipeRefreshLayout.isRefreshing = it is State.Loading
        }

        // Observing error to show toast with retry action
        observe(viewModel.errorData) {
            if (it !is PersistenceEmptyError) {
                showSnackbarWithAction(it) {
                    viewModel.retry()
                }
            }
        }

        observe(viewModel.pagedList) {
            launchesAdapter.submitList(it)
        }
    }

    private fun onItemSelected(item: LaunchItem) {
        openDetail(item.id)
    }

    override fun onRefresh() {
        viewModel.refresh()
    }

    override fun getLayoutId(): Int = R.layout.fragment_launches
    //endregion
}
