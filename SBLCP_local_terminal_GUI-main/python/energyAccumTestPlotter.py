import csv
import plotly.graph_objects as go
from datetime import datetime

def plot_real_time_data():
    x = []
    Yoko_phA_real_power = []
    Breaker_phA_real_power = []
    Yoko_phB_real_power = []
    Breaker_phB_real_power = []
    Percentage_difference = []  # New list for percentage difference

    fig_raw = go.Figure()  # Graph for raw numbers
    fig_diff = go.Figure()  # Graph for percentage difference

    fig_raw.add_trace(go.Scatter(x=x, y=Yoko_phA_real_power, name='Yoko_phA_real_power'))
    fig_raw.add_trace(go.Scatter(x=x, y=Breaker_phA_real_power, name='Breaker_phA_real_power'))
    fig_raw.add_trace(go.Scatter(x=x, y=Yoko_phB_real_power, name='Yoko_phB_real_power'))
    fig_raw.add_trace(go.Scatter(x=x, y=Breaker_phB_real_power, name='Breaker_phB_real_power'))

    fig_raw.update_layout(
        title='Energy Accum Test Data Plot (Raw Numbers)',
        xaxis_title='Time (ms)',
        yaxis_title='Readings (kWh)'
    )

    fig_diff.add_trace(go.Scatter(x=x, y=Percentage_difference, name='Percentage Difference'))

    fig_diff.update_layout(
        title='Energy Accum Test Data Plot (Percentage Difference)',
        xaxis_title='Time (ms)',
        yaxis_title='Percentage Difference (%)'
    )

    current_time = datetime.now().strftime("%Y%m%d_%H%M%S")

    with open('csv_output/energy_accum_test_result_{}.csv'.format(current_time), 'w', newline='') as csvfile:
        csvwriter = csv.writer(csvfile)
        csvwriter.writerow(['Time (ms)', 'Yoko phA real power val', 'Breaker phA real power val', 'Yoko phB real power val', 'Breaker phB real power val', 'Percentage Difference'])

        while True:
            user_input = input()    # Format has to be <Time in ms>,<Yoko_phA_real_power>,<Breaker_phA_real_power>,
                                    #                               <Yoko_phB_real_power>,<Breaker_phB_real_power>
                                    # eg: 1000,10.1,9.3,11.2,10.2
                                    

            if (user_input == "shutdown"): break
            if (user_input == "showplot"): 
                print("Hey! He asked me to show plot!")
                fig_raw.show()
                fig_diff.show()
                continue

            try:
                time, Yoko_phA_real_power_val, Breaker_phA_real_power_val, Yoko_phB_real_power_val, Breaker_phB_real_power_val = map(float, user_input.split(','))
                print("[DEBUG] energyAccumTestPlotter.py: Input successfully parsed.")
            except ValueError:
                print("Invalid input. Please try again.")

            x.append(time)
            Yoko_phA_real_power.append(Yoko_phA_real_power_val)
            Breaker_phA_real_power.append(Breaker_phA_real_power_val)
            Yoko_phB_real_power.append(Yoko_phB_real_power_val)
            Breaker_phB_real_power.append(Breaker_phB_real_power_val)

            # Calculate percentage difference
            percentage_diff = (Yoko_phA_real_power_val - Breaker_phA_real_power_val) / Breaker_phA_real_power_val * 100.0
            Percentage_difference.append(percentage_diff)

            fig_raw.data[0].x = x
            fig_raw.data[0].y = Yoko_phA_real_power
            fig_raw.data[1].x = x
            fig_raw.data[1].y = Breaker_phA_real_power
            fig_raw.data[2].x = x
            fig_raw.data[2].y = Yoko_phB_real_power
            fig_raw.data[3].x = x
            fig_raw.data[3].y = Breaker_phB_real_power

            fig_diff.data[0].x = x
            fig_diff.data[0].y = Percentage_difference

            csvwriter.writerow([time, Yoko_phA_real_power_val, Breaker_phA_real_power_val, Yoko_phB_real_power_val, Breaker_phB_real_power_val, percentage_diff])

if __name__ == '__main__':
    plot_real_time_data()
