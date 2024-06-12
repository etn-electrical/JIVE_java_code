import csv
import plotly.graph_objects as go

def plot_real_time_data():
    x = []
    y_yoko = []
    y_breaker = []

    fig = go.Figure()

    fig.add_trace(go.Scatter(x=x, y=y_yoko, name='Yoko Reading'))
    fig.add_trace(go.Scatter(x=x, y=y_breaker, name='Breaker Reading'))

    fig.update_layout(
        title='Real-Time Data Plot',
        xaxis_title='Time (ms)',
        yaxis_title='Reading (kWh)'
    )

    with open('data.csv', 'w', newline='') as csvfile:
        csvwriter = csv.writer(csvfile)
        csvwriter.writerow(['Time (ms)', 'Yoko Reading', 'Breaker Reading'])

        while True:
            user_input = input()    # Format has to be <Time in ms>,<Yoko reading>,<Breaker reading>, eg 1000,60.43,59.6

            if (user_input == "shutdown"): break
            if (user_input == "showplot"): fig.show()

            try:
                time, yoko_reading, breaker_reading = map(float, user_input.split(','))
            except ValueError:
                print("Invalid input. Please try again.")

            x.append(time)
            y_yoko.append(yoko_reading)
            y_breaker.append(breaker_reading)

            fig.data[0].x = x
            fig.data[0].y = y_yoko
            fig.data[1].x = x
            fig.data[1].y = y_breaker

            csvwriter.writerow([time, yoko_reading, breaker_reading])

if __name__ == '__main__':
    plot_real_time_data()
