package com.example.savesomebills;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class DeviceStorage {

    private static final String FILE_NAME = "devices.json";

    public static void save(Context context, Device device) {
        List<Device> devices = loadAll(context);
        devices.add(device);
        writeAll(context, devices);
    }

    public static void update(Context context, Device device) {
        List<Device> devices = loadAll(context);
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).objectId.equals(device.objectId)) {
                devices.set(i, device);
                break;
            }
        }
        writeAll(context, devices);
    }

    public static void delete(Context context, String objectId) {
        List<Device> devices = loadAll(context);
        devices.removeIf(d -> d.objectId.equals(objectId));
        writeAll(context, devices);
    }

    public static List<Device> loadAll(Context context) {
        List<Device> devices = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.openFileInput(FILE_NAME))
            );
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONArray array = new JSONArray(sb.toString());
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Device d = new Device(
                    obj.getString("name"),
                    obj.getString("groupId"),
                    (float) obj.getDouble("onHours"),
                    (float) obj.getDouble("standbyHours"),
                    obj.optInt("wattOn", 0),
                    obj.optInt("wattStandby", 0),
                    obj.optString("icon", "⚡")
                );
                d.objectId = obj.getString("objectId");
                devices.add(d);
            }
        } catch (FileNotFoundException e) {
            // No file yet — return empty list
        } catch (Exception e) {
            e.printStackTrace();
        }
        return devices;
    }

    private static void writeAll(Context context, List<Device> devices) {
        try {
            JSONArray array = new JSONArray();
            for (Device d : devices) {
                JSONObject obj = new JSONObject();
                obj.put("objectId", d.objectId);
                obj.put("groupId", d.groupId);
                obj.put("name", d.name);
                obj.put("icon", d.icon);
                obj.put("onHours", d.onHours);
                obj.put("standbyHours", d.standbyHours);
                obj.put("wattOn", d.wattOn);
                obj.put("wattStandby", d.wattStandby);
                array.put(obj);
            }
            OutputStreamWriter writer = new OutputStreamWriter(
                context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)
            );
            writer.write(array.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
