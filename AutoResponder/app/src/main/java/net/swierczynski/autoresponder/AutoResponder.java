package net.swierczynski.autoresponder;

import net.swierczynski.autoresponder.calls.UnreceivedCallsService;
import net.swierczynski.autoresponder.preferences.UserPreferences;
import net.swierczynski.autoresponder.texts.IncomingMsgsService;

import android.app.ActionBar;
import android.app.Activity;
import android.content.*;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

public class AutoResponder extends Activity {

	private final static int MENU_PREFERENCES = Menu.FIRST;
	private final static int MENU_RESET = 2;
	private final static int MENU_ABOUT = 3;
	
	private AutoResponderDbAdapter dbAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.main);
		
		dbAdapter = AutoResponderDbAdapter.initializeDatabase(this);
		registerCallsCheckboxListener();
		registerTextsCheckboxListener();
		displayProfilesSpinner();
		registerConfirmButtonListener();
		EditText ed1=(EditText)findViewById(R.id.callcount);

	}

	private void registerCallsCheckboxListener() {
		final CheckBox enabledCheckbox = (CheckBox) findViewById(R.id.enable_calls);
		enabledCheckbox.setChecked(UnreceivedCallsService.isActive);
		enabledCheckbox.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CheckBox cb = (CheckBox) v;
				boolean enabled = cb.isChecked();
				setServiceState(enabled, "calls");
			}

		});
	}
	
	private void registerTextsCheckboxListener() {
		final CheckBox enabledCheckbox = (CheckBox) findViewById(R.id.enable_texts);
		enabledCheckbox.setChecked(IncomingMsgsService.isActive);
		enabledCheckbox.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CheckBox cb = (CheckBox) v;
				boolean enabled = cb.isChecked();
				setServiceState(enabled, "texts");
			}
		});
	}

	private void displayProfilesSpinner() {
		Spinner profilesSpinner = (Spinner) findViewById(R.id.profile);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.profiles_array, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    profilesSpinner.setAdapter(adapter);
	    profilesSpinner.setSelection(adapter.getPosition(TxtMsgSender.getProfile()));
	    
	    registerProfilesSpinnerListener(profilesSpinner);
	}

	private void registerProfilesSpinnerListener(Spinner profilesSpinner) {
		profilesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String choosenProfile = parent.getItemAtPosition(pos).toString();
				TxtMsgSender.setProfile(choosenProfile);
				fillMessageBodyField();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// Do nothing
			}
		});
	}
	
	private void setServiceState(boolean enabled, String mode) {
		Intent service = new Intent(this, AutoResponderService.class);
		service.putExtra("isEnabled", enabled);
		service.putExtra("mode", mode);
		startService(service);
	}
	
	private void fillMessageBodyField() {
		String text = dbAdapter.fetchMessageBody(TxtMsgSender.getProfile());
		setMessageContent(text);
	}

	private void registerConfirmButtonListener() {
		Button confirmButton = (Button) findViewById(R.id.confirm);
		confirmButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String msgBody = getMessageContent();
				persistMessageContent(msgBody);
				showConfirmation();
			}

			private void showConfirmation() {
				Context context = getApplicationContext();
				String text = getText(R.string.message_saved) + " " + TxtMsgSender.getProfile();
				int duration = Toast.LENGTH_LONG;
				Toast confirmationMessage = Toast.makeText(context, text, duration);
				confirmationMessage.show();
			}
		});
	}
	
	private EditText getMessageBodyField() {
		return (EditText) findViewById(R.id.body);
	}
	
	private void setMessageContent(String content) {
		getMessageBodyField().setText(content);
	}
	
	private String getMessageContent() {
		return getMessageBodyField().getText().toString();
	}
	
	private void persistMessageContent(String content) {
		dbAdapter.saveMessage(TxtMsgSender.getProfile(), content);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		String content = getMessageContent();
		persistMessageContent(content);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbAdapter.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, MENU_PREFERENCES, 0, R.string.menu_preferences);
		menu.add(Menu.NONE, MENU_RESET, 1, R.string.menu_reset);
		menu.add(Menu.NONE, MENU_ABOUT, 2, R.string.menu_about);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch(item.getItemId()) {
			case MENU_PREFERENCES: {
				Intent i = new Intent(this, UserPreferences.class);
				startActivity(i);
				return true;
			}
			case MENU_RESET: {
				Intent intent = new Intent(NotificationArea.RESET);
				sendBroadcast(intent);
				return true;
			}
			case MENU_ABOUT: {
				Intent i = new Intent(this, About.class);
				startActivity(i);
				return true;
			}
		}
		
		return false;
	}

}