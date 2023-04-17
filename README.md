# nanoRedux
NanoRedux for Android (MVI)


# Profits
1. not threads race
2. log your states and actions for debug
3. easy to use on views and compose
4. easy to delete from project
5. tiny size



# Use
1. Create your store class: 'MyStore: ReduxStoreViewModel<MyDataStore.State, MyDataStore.Action, NanoRedux.Effect>() ...'
2. Override method doAction
3. In view get your store as viewModel
4. Listenen store.observeState() and update your view
5. Call store.dispatch() on your view callbacks (onClick, OnChangeText...)
