package moe.ku6.yukireplay.api.recorder;

import moe.ku6.yukireplay.api.codec.Instruction;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.IOException;

public interface IRecorder {
    void AddChunks(Chunk... chunks);
    void AddPlayers(Player... players);
    void RemovePlayers(Player... players);
    void StartRecording();
    void StopRecording();
    void SetRecording(boolean recording);
    void Close();
    byte[] Serialize(boolean flush) throws IOException;
    void ScheduleInstruction(Instruction instruction);
    int GetTrackerId(Entity entity);
}
