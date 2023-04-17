# nanoRedux
NanoRedux for Android (MVI)

Example:

#
    class MyDataStore : ReduxStoreViewModel<MyDataStore.State, MyDataStore.Action, NanoRedux.Effect>() {
      private val myDataRepository: MyDataRepository by inject()

      sealed interface State : ReduxStore.State {
          object Loading: State

          data class Loaded(
              val text: String,
          ) : State

          data class LoadingError(
              val errorMessage: String,
          ) : Action
      }

      sealed interface Action : ReduxStore.Action {
          object Start : Action

          data class DataLoaded(
              val text: String
          ) : Action

          data class DataLoadingError(
              val errorMessage: String,
          ) : Action

          object RetryLoading: String
      }

      override val logTag: String = "MyDataStore"

      init {
          dispatch(Action.Start)
      }

      override fun doAction(action: Action, oldState: State?): State? {
          return when (action) {
              Action.Start -> loading()

              is Action.DataLoaded -> State.Loaded(action.text)

              is Action.DataLoadingError -> State.LoadingError(action.errorMessage)

              Action.RetryLoading -> loading()
          }
      }

      private fun loading(): State? {
          launch(Dispatchers.IO) {
              runCatching {
                  val data = myDataRepository.getData()
                  dispatch(Action.DataLoaded(data))
              }.onFailure {
                  dispatch(Action.LoadingError(it.message))
              }
          }
          return State.Loading
      }

    }

#
